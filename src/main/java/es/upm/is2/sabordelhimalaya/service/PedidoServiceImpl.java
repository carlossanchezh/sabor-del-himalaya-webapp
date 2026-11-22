package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.model.LineaPedidoMenuDiario;
import es.upm.is2.sabordelhimalaya.model.LineaPedidoPlato;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.model.Usuario;

import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.dto.session.PedidoTemporalDTO;

import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;

import es.upm.is2.sabordelhimalaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlatoRepository platoRepository;
    private final MenuDiarioRepository menuDiarioRepository;
    private final MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    private final LineaPedidoPlatoRepository lineaPedidoPlatoRepository;
    private final LineaPedidoMenuDiarioRepository lineaPedidoMenuDiarioRepository;

    @Override
    @Transactional
    public Pedido crearPedido(Long idUsuario,String direccionEntrega, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {

        // Validación inicial de las listas
        List<LineaPedidoPlatoDTO> platos = (lineasPlato != null) ? lineasPlato : List.of();
        List<LineaPedidoMenuDiarioDTO> menus = (lineasMenu != null) ? lineasMenu : List.of();

        // Si el pedido está vacío, excepción
        if (platos.isEmpty() && menus.isEmpty()) {
            throw new IllegalArgumentException("El pedido está vacío. Añade algún plato o menú del día.");
        }

        // Si la dirección de entrega es nula, excepción
        if (direccionEntrega == null || direccionEntrega.isBlank()) {
            throw new IllegalArgumentException("Debes indicar la dirección de entrega.");
        }

        // Si la dirección de entrega es excesivamente larga, excepción
        if (direccionEntrega.trim().length() > 255) {
            throw new IllegalArgumentException("La dirección de entrega no puede superar los 255 caracteres.");
        }

        // Validar que existe el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + idUsuario + "."));

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(direccionEntrega.trim());
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.RECIBIDO);

        // Validar las líneas de pedido
        BigDecimal total = BigDecimal.ZERO;
        List<LineaPedidoPlato> lineasPlatoPedido = new ArrayList<>();
        for (LineaPedidoPlatoDTO dto : platos) {
            LineaPedidoPlato linea = construirLineaPlato(dto);
            linea.setPedido(pedido);
            lineasPlatoPedido.add(linea);
            total = total.add(subtotal(linea.getPrecioUnitario(), linea.getCantidad()));
        }
        List<LineaPedidoMenuDiario> lineasMenuPedido = new ArrayList<>();
        for (LineaPedidoMenuDiarioDTO dto : menus) {
            LineaPedidoMenuDiario linea = construirLineaMenu(dto);
            linea.setPedido(pedido);
            lineasMenuPedido.add(linea);
            total = total.add(subtotal(linea.getPrecioUnitario(), linea.getCantidad()));
        }

        // Asigna el importe total al pedido
        pedido.setImporteTotal(total.setScale(2, RoundingMode.HALF_UP));

        // Guardamos el pedido y las líneas del pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        lineaPedidoPlatoRepository.saveAll(lineasPlatoPedido);
        lineaPedidoMenuDiarioRepository.saveAll(lineasMenuPedido);

        return pedidoGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenPedidoDTO resumirPedidoTemporal(List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {

        List<ResumenPedidoDTO.Linea> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Procesar las líneas de pedido de menús del día
        List<LineaPedidoMenuDiarioDTO> menus = (lineasMenu != null) ? lineasMenu : List.of();
        for (int i = 0; i < menus.size(); i++) {

            // Validar la línea
            LineaPedidoMenuDiario linea = construirLineaMenu(menus.get(i));
            String detalle = linea.getEntrante().getNombre() + " · " + linea.getPrincipal().getNombre() + " · " + linea.getPostre().getNombre() + " · " + linea.getBebida().getNombre();
            BigDecimal subtotal = subtotal(linea.getPrecioUnitario(), linea.getCantidad());

            // Añadir línea al resumen
            lineas.add(new ResumenPedidoDTO.Linea(true, i, "Menú del día", detalle, linea.getCantidad(), linea.getPrecioUnitario(), subtotal));
            total = total.add(subtotal);
        }

        // Procesar las líneas de pedido de productos
        List<LineaPedidoPlatoDTO> platos = (lineasPlato != null) ? lineasPlato : List.of();
        for (int i = 0; i < platos.size(); i++) {

            // Validar la línea
            LineaPedidoPlato linea = construirLineaPlato(platos.get(i));
            BigDecimal subtotal = subtotal(linea.getPrecioUnitario(), linea.getCantidad());

            // Añadir línea al resumen
            lineas.add(new ResumenPedidoDTO.Linea(false, i, linea.getPlato().getNombre(), null, linea.getCantidad(), linea.getPrecioUnitario(), subtotal));
            total = total.add(subtotal);
        }

        // Devolver resumen del pedido
        return new ResumenPedidoDTO(lineas, total.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public ResumenPedidoDTO obtenerResumenDePedido(Long idPedido) {

        // Obtener el pedido
        Pedido pedido = buscarPorId(idPedido);

        // Líneas del resumen del pedido
        List<ResumenPedidoDTO.Linea> lineas = new ArrayList<>();

        // Obtenemos las líneas de platos del pedido desde la BD
        List<LineaPedidoPlato> platos = lineaPedidoPlatoRepository.findByPedidoIdPedido(idPedido);
        for (int i = 0; i < platos.size(); i++) {
            LineaPedidoPlato lineaPlato = platos.get(i);
            lineas.add(new ResumenPedidoDTO.Linea(false, i, lineaPlato.getPlato().getNombre(), null, lineaPlato.getCantidad(), lineaPlato.getPrecioUnitario(), subtotal(lineaPlato.getPrecioUnitario(), lineaPlato.getCantidad())));
        }

        // Obtenemos las líneas de menús diarios del pedido desde la BD
        List<LineaPedidoMenuDiario> menus = lineaPedidoMenuDiarioRepository.findByPedidoIdPedido(idPedido);
        for (int i = 0; i < menus.size(); i++) {
            LineaPedidoMenuDiario lineaMenu = menus.get(i);
            String detalle = lineaMenu.getEntrante().getNombre() + " · " + lineaMenu.getPrincipal().getNombre() + " · " + lineaMenu.getPostre().getNombre() + " · " + lineaMenu.getBebida().getNombre();
            lineas.add(new ResumenPedidoDTO.Linea(true, i, "Menú del día", detalle, lineaMenu.getCantidad(), lineaMenu.getPrecioUnitario(), subtotal(lineaMenu.getPrecioUnitario(), lineaMenu.getCantidad())));
        }

        // Calculamos el total (o podemos usar pedidoRepository.findById(idPedido).get().getImporteTotal())
        BigDecimal total = pedido.getImporteTotal();

        return new ResumenPedidoDTO(lineas, total.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public List<Pedido> listarPedidosDeUsuario(Long idUsuario) {

        //Obtener pedidos del usuario
        return pedidoRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(idUsuario);
    }

    @Override
    @Transactional
    public Pedido modificarPedido(Long idPedido, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {

        // Se busca el pedido (lanza RecursoNoEncontradoException si no existe)
        Pedido pedido = buscarPorId(idPedido);

        // CA9.3: pasados los 10 minutos (o si está cancelado) no se puede modificar
        if (!sePuedeModificar(pedido)) {
            throw new OperacionNoPermitidaException("El pedido #" + idPedido + " ya no se puede modificar: han pasado más de " + MINUTOS_MODIFICACION + " minutos desde que se realizó.");
        }

        // Validación inicial de las listas
        List<LineaPedidoPlatoDTO> platos = (lineasPlato != null) ? lineasPlato : List.of();
        List<LineaPedidoMenuDiarioDTO> menus = (lineasMenu != null) ? lineasMenu : List.of();

        // Un pedido modificado no puede quedarse vacío
        if (platos.isEmpty() && menus.isEmpty()) {
            throw new IllegalArgumentException("El pedido no puede quedarse vacío. Si ya no lo quieres, elimínalo.");
        }

        // Se validan y construyen las nuevas líneas, recalculando el total
        BigDecimal total = BigDecimal.ZERO;
        List<LineaPedidoPlato> nuevasLineasPlato = new ArrayList<>();
        for (LineaPedidoPlatoDTO dto : platos) {
            LineaPedidoPlato linea = construirLineaPlato(dto);
            linea.setPedido(pedido);
            nuevasLineasPlato.add(linea);
            total = total.add(subtotal(linea.getPrecioUnitario(), linea.getCantidad()));
        }
        List<LineaPedidoMenuDiario> nuevasLineasMenu = new ArrayList<>();
        for (LineaPedidoMenuDiarioDTO dto : menus) {
            LineaPedidoMenuDiario linea = construirLineaMenu(dto);
            linea.setPedido(pedido);
            nuevasLineasMenu.add(linea);
            total = total.add(subtotal(linea.getPrecioUnitario(), linea.getCantidad()));
        }

        // Se borran las líneas antiguas y se guardan las nuevas
        lineaPedidoPlatoRepository.deleteAll(lineaPedidoPlatoRepository.findByPedidoIdPedido(idPedido));
        lineaPedidoMenuDiarioRepository.deleteAll(lineaPedidoMenuDiarioRepository.findByPedidoIdPedido(idPedido));
        lineaPedidoPlatoRepository.saveAll(nuevasLineasPlato);
        lineaPedidoMenuDiarioRepository.saveAll(nuevasLineasMenu);

        // CA9.2: se actualiza el importe del pedido (la fecha no cambia: el plazo cuenta desde su realización)
        pedido.setImporteTotal(total.setScale(2, RoundingMode.HALF_UP));

        return pedidoRepository.save(pedido);
    }

    @Override
    public boolean sePuedeModificar(Pedido pedido) {

        return pedido.getEstado() != EstadoPedido.CANCELADO
                && pedido.getFechaHora() != null
                && LocalDateTime.now().isBefore(pedido.getFechaHora().plusMinutes(MINUTOS_MODIFICACION));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoTemporalDTO obtenerLineasDePedido(Long idPedido) {

        // Comprueba que el pedido existe
        buscarPorId(idPedido);

        PedidoTemporalDTO lineas = new PedidoTemporalDTO();

        // Líneas de platos
        for (LineaPedidoPlato linea : lineaPedidoPlatoRepository.findByPedidoIdPedido(idPedido)) {
            LineaPedidoPlatoDTO dto = new LineaPedidoPlatoDTO();
            dto.setIdPlato(linea.getPlato().getIdPlato());
            dto.setCantidad(linea.getCantidad());
            lineas.getLineasPlato().add(dto);
        }

        // Líneas de menús del día
        for (LineaPedidoMenuDiario linea : lineaPedidoMenuDiarioRepository.findByPedidoIdPedido(idPedido)) {
            LineaPedidoMenuDiarioDTO dto = new LineaPedidoMenuDiarioDTO();
            dto.setIdMenuDiario(linea.getMenuDiario().getIdMenuDiario());
            dto.setIdEntrante(linea.getEntrante().getIdPlato());
            dto.setIdPrincipal(linea.getPrincipal().getIdPlato());
            dto.setIdPostre(linea.getPostre().getIdPlato());
            dto.setIdBebida(linea.getBebida().getIdPlato());
            dto.setCantidad(linea.getCantidad());
            lineas.getLineasMenu().add(dto);
        }

        return lineas;
    }

    @Override
    @Transactional
    public void cancelarPedido(Long idPedido) {

        // Se busca el pedido (lanza RecursoNoEncontradoException si no existe)
        Pedido pedido = buscarPorId(idPedido);

        // Un pedido ya cancelado no se puede volver a eliminar
        // (este control va antes que el del plazo, porque sePuedeModificar también devuelve false si está cancelado)
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new OperacionNoPermitidaException("El pedido #" + idPedido + " ya está cancelado.");
        }

        // CA10.3: pasados los 10 minutos no se puede eliminar
        if (!sePuedeModificar(pedido)) {
            throw new OperacionNoPermitidaException("El pedido #" + idPedido + " ya no se puede eliminar: han pasado más de " + MINUTOS_MODIFICACION + " minutos desde que se realizó.");
        }

        // CA10.1 y CA10.2: el pedido pasa a CANCELADO y deja de aparecer como pedido activo
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {

        // El nuevo estado es obligatorio
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Se debe indicar el nuevo estado del pedido.");
        }

        // Se busca el pedido (lanza RecursoNoEncontradoException si no existe)
        Pedido pedido = buscarPorId(idPedido);

        // Se actualiza y se almacena el nuevo estado
        pedido.setEstado(nuevoEstado);

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosDelDia(LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException("Se debe indicar la fecha de la que listar los pedidos.");
        }

        // Pedidos cuyo fechaHora está dentro del día indicado
        return pedidoRepository.findByFechaHoraBetween(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }

    @Override
    public BigDecimal calcularFacturacion(LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException("Se debe indicar la fecha de la que calcular la facturación.");
        }

        // Obtiene todos los pedidos entregados y que están en el rango de la fecha
        List<Pedido> pedidosFacturacion = pedidoRepository.findByEstadoAndFechaHoraBetween(EstadoPedido.ENTREGADO, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());

        BigDecimal total = BigDecimal.ZERO;

        // Calcula la suma total de todos los pedidos
        for (Pedido pedido : pedidosFacturacion) {
            total = total.add(pedido.getImporteTotal());
        }

        // Redondea la facturación total
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Pedido buscarPorId(Long id) {
        // Obtener un pedido por su id
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + id));
    }

    @Override
    public List<Pedido> listarTodosPedidos() {
        // TODO: return pedidoRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    // Convierte un LineaPedidoPlatoDTO a LineaPedidoPlato
    private LineaPedidoPlato construirLineaPlato(LineaPedidoPlatoDTO dto) {

        if (dto == null || dto.getIdPlato() == null) {
            throw new IllegalArgumentException("Hay un plato sin indicar en el pedido.");
        }

        validarCantidad(dto.getCantidad());

        // Buscamos el plato en la base de datos para validar su existencia y precio actual
        Plato plato = platoRepository.findById(dto.getIdPlato())
                .orElseThrow(() -> new IllegalArgumentException("El plato seleccionado ya no existe."));

        // No se puede pedir un plato que el dueño haya retirado
        if (!Boolean.TRUE.equals(plato.getEstaEnCarta())) {
            throw new IllegalArgumentException("El plato '" + plato.getNombre() + "' ya no está en la carta.");
        }

        // Crear la Línea de Pedido del producto
        LineaPedidoPlato linea = new LineaPedidoPlato();
        linea.setPlato(plato);
        linea.setCantidad(dto.getCantidad());
        linea.setPrecioUnitario(plato.getPrecio());

        return linea;
    }

    // Convierte un LineaPedidoMenuDiarioDTO a LineaPedidoMenuDiario
    private LineaPedidoMenuDiario construirLineaMenu(LineaPedidoMenuDiarioDTO dto) {

        if (dto == null || dto.getIdMenuDiario() == null || dto.getIdEntrante() == null || dto.getIdPrincipal() == null || dto.getIdPostre() == null || dto.getIdBebida() == null) {
            throw new IllegalArgumentException("Para añadir el menú del día elige un entrante, un principal, un postre y una bebida.");
        }

        validarCantidad(dto.getCantidad());

        // Validar que el menú existe
        MenuDiario menu = menuDiarioRepository.findById(dto.getIdMenuDiario())
                .orElseThrow(() -> new IllegalArgumentException("El menú del día seleccionado ya no existe."));

        // Validar que el menú es de la fecha actual
        if (!LocalDate.now().equals(menu.getFecha())) {
            throw new IllegalArgumentException("Solo se puede pedir el menú del día de hoy.");
        }

        // Validar que la bebida elegida es de categoría BEBIDA y está en la carta
        Plato bebida = platoRepository.findById(dto.getIdBebida())
                .filter(p -> p.getCategoria() == CategoriaPlato.BEBIDA && Boolean.TRUE.equals(p.getEstaEnCarta()))
                .orElseThrow(() -> new IllegalArgumentException("La bebida elegida no está disponible."));


        // Crear la Línea de Pedido del menú
        LineaPedidoMenuDiario linea = new LineaPedidoMenuDiario();
        linea.setMenuDiario(menu);
        linea.setEntrante(opcionDelMenu(menu, dto.getIdEntrante(), CategoriaMenu.ENTRANTE, "entrante"));
        linea.setPrincipal(opcionDelMenu(menu, dto.getIdPrincipal(), CategoriaMenu.PRINCIPAL, "principal"));
        linea.setPostre(opcionDelMenu(menu, dto.getIdPostre(), CategoriaMenu.POSTRE, "postre"));
        linea.setBebida(bebida);
        linea.setCantidad(dto.getCantidad());
        linea.setPrecioUnitario(menu.getPrecio());

        return linea;
    }

    // Comprueba si un plato específico es una de las opciones configuradas del menú para una categoría concreta
    private Plato opcionDelMenu(MenuDiario menu, Long idPlato, CategoriaMenu categoria, String nombreGrupo) {

        return menuDiarioOpcionRepository.findByMenuDiarioIdMenuDiarioAndCategoria(menu.getIdMenuDiario(), categoria)
                .stream()
                .map(MenuDiarioOpcion::getPlato)
                .filter(plato -> plato.getIdPlato().equals(idPlato))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El " + nombreGrupo + " elegido no es una opción del menú de hoy."));
    }

    // Validación básica de cantidades para evitar negativas o nulas
    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de cada producto debe ser al menos 1.");
        }
    }

    // Calcula subtotal precio x cantidad
    private BigDecimal subtotal(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
