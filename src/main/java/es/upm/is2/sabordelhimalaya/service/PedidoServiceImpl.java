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

import es.upm.is2.sabordelhimalaya.dto.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.dto.ResumenCarritoDTO;

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

    // US-06 / US-07: Registrar el pedido confirmado con sus líneas y el importe total
    @Override
    @Transactional
    public Pedido crearPedido(Long idUsuario,String direccionEntrega, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {
        List<LineaPedidoPlatoDTO> platos = (lineasPlato != null) ? lineasPlato : List.of();
        List<LineaPedidoMenuDiarioDTO> menus = (lineasMenu != null) ? lineasMenu : List.of();

        if (platos.isEmpty() && menus.isEmpty()) {
            throw new IllegalArgumentException("El pedido está vacío. Añade algún plato o menú del día.");
        }
        if (direccionEntrega == null || direccionEntrega.isBlank()) {
            throw new IllegalArgumentException("Debes indicar la dirección de entrega.");
        }
        if (direccionEntrega.trim().length() > 255) {
            throw new IllegalArgumentException("La dirección de entrega no puede superar los 255 caracteres.");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + idUsuario + "."));
        // CA2.3: una cuenta no activada no puede realizar pedidos
        if (!Boolean.TRUE.equals(usuario.getEstadoActivacion())) {
            throw new OperacionNoPermitidaException("Debes activar tu cuenta antes de realizar pedidos.");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(direccionEntrega.trim());
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.RECIBIDO);

        // Se validan y calculan todas las líneas antes de guardar nada
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
        pedido.setImporteTotal(total.setScale(2, RoundingMode.HALF_UP));

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        lineaPedidoPlatoRepository.saveAll(lineasPlatoPedido);
        lineaPedidoMenuDiarioRepository.saveAll(lineasMenuPedido);
        return pedidoGuardado;
    }

    // US-06 (CA6.5) / US-07 (CA7.1, CA7.2): mismos cálculos que crearPedido pero sin guardar nada
    @Override
    @Transactional(readOnly = true)
    public ResumenCarritoDTO resumirCarrito(List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {
        List<ResumenCarritoDTO.Linea> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        List<LineaPedidoMenuDiarioDTO> menus = (lineasMenu != null) ? lineasMenu : List.of();
        for (int i = 0; i < menus.size(); i++) {
            LineaPedidoMenuDiario linea = construirLineaMenu(menus.get(i));
            String detalle = linea.getEntrante().getNombre() + " · " + linea.getPrincipal().getNombre() + " · "
                    + linea.getPostre().getNombre() + " · " + linea.getBebida().getNombre();
            BigDecimal subtotal = subtotal(linea.getPrecioUnitario(), linea.getCantidad());
            lineas.add(new ResumenCarritoDTO.Linea(true, i, "Menú del día", detalle,
                    linea.getCantidad(), linea.getPrecioUnitario(), subtotal));
            total = total.add(subtotal);
        }

        List<LineaPedidoPlatoDTO> platos = (lineasPlato != null) ? lineasPlato : List.of();
        for (int i = 0; i < platos.size(); i++) {
            LineaPedidoPlato linea = construirLineaPlato(platos.get(i));
            BigDecimal subtotal = subtotal(linea.getPrecioUnitario(), linea.getCantidad());
            lineas.add(new ResumenCarritoDTO.Linea(false, i, linea.getPlato().getNombre(), null,
                    linea.getCantidad(), linea.getPrecioUnitario(), subtotal));
            total = total.add(subtotal);
        }

        return new ResumenCarritoDTO(lineas, total.setScale(2, RoundingMode.HALF_UP));
    }

    // US-08: pedidos del usuario, del más reciente al más antiguo, con su estado actual
    @Override
    public List<Pedido> listarPedidosDeUsuario(Long idUsuario) {
        return pedidoRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(idUsuario);
    }

    @Override
    public Pedido modificarPedido(Long idPedido, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {
        // TODO (US-09): Validar que no hayan pasado 10 min. Borrar líneas antiguas, crear nuevas, recalcular total.
        throw new UnsupportedOperationException("TODO US-09");
    }

    @Override
    public void cancelarPedido(Long idPedido) {
        // TODO (US-10): Validar que no hayan pasado 10 min. Cambiar estado a CANCELADO.
        throw new UnsupportedOperationException("TODO US-10");
    }

    @Override
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        // TODO (US-11): Buscar pedido, actualizar estado y guardar.
        throw new UnsupportedOperationException("TODO US-11");
    }

    @Override
    public List<Pedido> listarPedidosDelDia(LocalDate fecha) {
        // TODO (US-11): return pedidoRepository.findByFechaHoraBetween(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        throw new UnsupportedOperationException("TODO US-11");
    }

    @Override
    public BigDecimal calcularFacturacion(LocalDate fecha) {
        // TODO (US-19): Obtener pedidos del día y sumar importeTotal (excluyendo cancelados).
        throw new UnsupportedOperationException("TODO US-19");
    }

    @Override
    public Pedido buscarPorId(Long id) {
        // TODO: return pedidoRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Pedido> listarTodosPedidos() {
        // TODO: return pedidoRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }

    // ----------------------------------------------------------------
    // Métodos auxiliares de crearPedido y resumirCarrito (US-06 / US-07)
    // ----------------------------------------------------------------

    // Línea de un plato de la carta con su precio actual (sin pedido asignado)
    private LineaPedidoPlato construirLineaPlato(LineaPedidoPlatoDTO dto) {
        if (dto == null || dto.getIdPlato() == null) {
            throw new IllegalArgumentException("Hay un plato sin indicar en el pedido.");
        }
        validarCantidad(dto.getCantidad());
        Plato plato = platoRepository.findById(dto.getIdPlato())
                .orElseThrow(() -> new IllegalArgumentException("El plato seleccionado ya no existe."));
        if (!Boolean.TRUE.equals(plato.getEstaEnCarta())) {
            throw new IllegalArgumentException("El plato '" + plato.getNombre() + "' ya no está en la carta.");
        }

        LineaPedidoPlato linea = new LineaPedidoPlato();
        linea.setPlato(plato);
        linea.setCantidad(dto.getCantidad());
        linea.setPrecioUnitario(plato.getPrecio());
        return linea;
    }

    // CA6.2: el entrante, el principal y el postre deben ser opciones del menú de hoy; la bebida, una bebida de la carta
    private LineaPedidoMenuDiario construirLineaMenu(LineaPedidoMenuDiarioDTO dto) {
        if (dto == null || dto.getIdMenuDiario() == null || dto.getIdEntrante() == null
                || dto.getIdPrincipal() == null || dto.getIdPostre() == null || dto.getIdBebida() == null) {
            throw new IllegalArgumentException("Para añadir el menú del día elige un entrante, un principal, un postre y una bebida.");
        }
        validarCantidad(dto.getCantidad());
        MenuDiario menu = menuDiarioRepository.findById(dto.getIdMenuDiario())
                .orElseThrow(() -> new IllegalArgumentException("El menú del día seleccionado ya no existe."));
        if (!LocalDate.now().equals(menu.getFecha())) {
            throw new IllegalArgumentException("Solo se puede pedir el menú del día de hoy.");
        }

        Plato bebida = platoRepository.findById(dto.getIdBebida())
                .filter(p -> p.getCategoria() == CategoriaPlato.BEBIDA && Boolean.TRUE.equals(p.getEstaEnCarta()))
                .orElseThrow(() -> new IllegalArgumentException("La bebida elegida no está disponible."));

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

    private Plato opcionDelMenu(MenuDiario menu, Long idPlato, CategoriaMenu categoria, String nombreGrupo) {
        return menuDiarioOpcionRepository.findByMenuDiarioIdMenuDiarioAndCategoria(menu.getIdMenuDiario(), categoria)
                .stream()
                .map(MenuDiarioOpcion::getPlato)
                .filter(plato -> plato.getIdPlato().equals(idPlato))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El " + nombreGrupo + " elegido no es una opción del menú de hoy."));
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de cada producto debe ser al menos 1.");
        }
    }

    private BigDecimal subtotal(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
