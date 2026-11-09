package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.session.PedidoTemporalDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.service.PedidoService;
import es.upm.is2.sabordelhimalaya.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidosController {

    // Textos asociados a cada estado del pedido
    private static final Map<EstadoPedido, String> NOMBRES_ESTADO = new LinkedHashMap<>();
    static {
        NOMBRES_ESTADO.put(EstadoPedido.RECIBIDO, "Recibido");
        NOMBRES_ESTADO.put(EstadoPedido.EN_PREPARACION, "En preparación");
        NOMBRES_ESTADO.put(EstadoPedido.EN_REPARTO, "En reparto");
        NOMBRES_ESTADO.put(EstadoPedido.ENTREGADO, "Entregado");
        NOMBRES_ESTADO.put(EstadoPedido.CANCELADO, "Cancelado");
    }

    // Pasos de la barra de progreso de cada pedido
    private static final List<EstadoPedido> PASOS_SEGUIMIENTO = List.of(
            EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO);

    // Máximo de unidades por línea de pedido
    private static final int MAX_CANTIDAD = 99;

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    @PostMapping("/anadir-plato")
    public String anadirPlato(@RequestParam Long idPlato, @RequestParam(defaultValue = "1") Integer cantidad, @RequestParam(defaultValue = "cat-0") String tabActiva, HttpSession session, RedirectAttributes redirectAttributes) {

        // Se conserva la pestaña de la carta desde la que se ha añadido el producto
        redirectAttributes.addAttribute("tabActiva", tabActiva);

        PedidoTemporalDTO pedidoTemporal = PedidoTemporalDTO.deSesion(session);

        // No se permiten menos de 1 ni más de 99 unidades por línea de pedido
        int yaEnPedido = pedidoTemporal.getLineasPlato().stream()
                .filter(l -> l.getIdPlato().equals(idPlato))
                .mapToInt(LineaPedidoPlatoDTO::getCantidad)
                .sum();

        if (cantidad == null || cantidad < 1 || cantidad > MAX_CANTIDAD - yaEnPedido) {

            redirectAttributes.addFlashAttribute("error", "La cantidad de cada producto debe ser entre 1 y " + MAX_CANTIDAD + " unidades.");
            return "redirect:/carta";
        }

        // Crea la línea de pedido del producto añadido
        LineaPedidoPlatoDTO linea = new LineaPedidoPlatoDTO();
        linea.setIdPlato(idPlato);
        linea.setCantidad(cantidad);

        try {

            // Comprueba que el plato existe, está en carta y la cantidad es válida antes de meterlo en el pedido temporal
            pedidoService.resumirPedidoTemporal(List.of(linea), List.of());

        } catch (IllegalArgumentException e) {

            // Si hay algún error lo informa
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carta";
        }

        // Se añade la línea de pedido del producto al pedido de la sesión
        pedidoTemporal.anadirPlato(linea);

        return "redirect:/carta";
    }

    @PostMapping("/anadir-menu")
    public String anadirMenu(@Valid @ModelAttribute LineaPedidoMenuDiarioDTO linea, BindingResult bindingResult, @RequestParam(defaultValue = "menu-del-dia") String tabActiva, HttpSession session, RedirectAttributes redirectAttributes) {

        // Se conserva la pestaña del menú del día
        redirectAttributes.addAttribute("tabActiva", tabActiva);

        // Cantidad fuera de rango
        if (bindingResult.hasFieldErrors("cantidad")) {

            redirectAttributes.addFlashAttribute("error", "La cantidad de cada producto debe ser entre 1 y " + MAX_CANTIDAD + " unidades.");
            return "redirect:/carta";
        }

        // Si el formulario de selección del menú del día tiene errores se informa
        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("error", "Para añadir el menú del día elige un entrante, un principal, un postre y una bebida.");
            return "redirect:/carta";
        }

        PedidoTemporalDTO pedidoTemporal = PedidoTemporalDTO.deSesion(session);

        // No se permiten más de 99 menús con la misma selección
        int yaEnPedido = pedidoTemporal.getLineasMenu().stream()
                .filter(l -> Objects.equals(l.getIdMenuDiario(), linea.getIdMenuDiario())
                        && Objects.equals(l.getIdEntrante(), linea.getIdEntrante())
                        && Objects.equals(l.getIdPrincipal(), linea.getIdPrincipal())
                        && Objects.equals(l.getIdPostre(), linea.getIdPostre())
                        && Objects.equals(l.getIdBebida(), linea.getIdBebida()))
                .mapToInt(LineaPedidoMenuDiarioDTO::getCantidad)
                .sum();

        if (linea.getCantidad() > MAX_CANTIDAD - yaEnPedido) {

            redirectAttributes.addFlashAttribute("error", "La cantidad de cada producto debe ser entre 1 y " + MAX_CANTIDAD + " unidades.");
            return "redirect:/carta";
        }

        try {

            // Comprueba que los platos elegidos son opciones válidas del menú de hoy
            pedidoService.resumirPedidoTemporal(List.of(), List.of(linea));

        } catch (IllegalArgumentException e) {

            // Si hay algún error lo informa
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carta";
        }

        // Se añade la línea de pedido de menú del día al pedido de la sesión
        pedidoTemporal.anadirMenu(linea);

        return "redirect:/carta";
    }

    @GetMapping("/revisar")
    public String revisarPedido(HttpSession session, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {

        // Si el pedido temporal está vacío se redirige a la carta y se informa
        if (PedidoTemporalDTO.deSesion(session).estaVacio()) {
            String errorPedidoTemporal = (String) model.getAttribute("errorPedidoTemporal");
            redirectAttributes.addFlashAttribute("error", errorPedidoTemporal != null ? errorPedidoTemporal : "Tu pedido está vacío. Añade platos de la carta o un menú del día.");
            return "redirect:/carta";
        }

        // Obtenemos los datos del usuario logueado
        Usuario usuario = usuarioService.buscarPorLogin(authentication.getName());
        model.addAttribute("nombreUsuario", usuario.getNombre());

        // Si la direccion de entrega es eliminada se restablece la del usuario por defecto
        if (!model.containsAttribute("direccionEntrega")) {
            model.addAttribute("direccionEntrega", usuario.getDireccion());
        }

        return "pedidos/revisar";
    }

    @GetMapping("/editar-pedido-temporal")
    public String mostrarEditarPedidoTemporal(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        // Si el pedido temporal está vacío se redirige a la carta y se informa
        if (PedidoTemporalDTO.deSesion(session).estaVacio()) {
            String errorPedidoTemporal = (String) model.getAttribute("errorPedidoTemporal");
            redirectAttributes.addFlashAttribute("error", errorPedidoTemporal != null ? errorPedidoTemporal : "Tu pedido está vacío. Añade platos de la carta o un menú del día.");
            return "redirect:/carta";
        }

        return "pedidos/editar";
    }

    @PostMapping("/editar-pedido-temporal")
    public String guardarCambiosPedidoTemporal(@RequestParam(required = false) List<Integer> cantidadesPlato, @RequestParam(required = false) List<Integer> cantidadesMenu, @RequestParam(required = false) Integer eliminarPlato, @RequestParam(required = false) Integer eliminarMenu, HttpSession session, RedirectAttributes redirectAttributes) {

        // Recuperamos el pedido temporal de la sesión
        PedidoTemporalDTO pedidoTemporal = PedidoTemporalDTO.deSesion(session);

        // Se obtienen las líneas de pedido del pedido temporal de sesión
        List<LineaPedidoPlatoDTO> platos = pedidoTemporal.getLineasPlato();
        List<LineaPedidoMenuDiarioDTO> menus = pedidoTemporal.getLineasMenu();

        int numPlatos = (cantidadesPlato != null) ? cantidadesPlato.size() : 0;
        int numMenus = (cantidadesMenu != null) ? cantidadesMenu.size() : 0;

        // Se comprueba que el número de inputs recibidos coincide con las líneas reales del pedido
        if (numPlatos != platos.size() || numMenus != menus.size() || (cantidadesPlato != null && cantidadesPlato.contains(null)) || (cantidadesMenu != null && cantidadesMenu.contains(null))) {

            // El pedido temporal ha cambiado desde que se abrió la pantalla (p. ej. en otra pestaña) o falta alguna cantidad
            redirectAttributes.addFlashAttribute("error", "No se han podido guardar los cambios. Revisa las cantidades e inténtalo de nuevo.");
            return "redirect:/pedidos/editar-pedido-temporal";
        }

        // No se permiten más de 99 unidades por línea de pedido
        if ((cantidadesPlato != null && cantidadesPlato.stream().anyMatch(c -> c > MAX_CANTIDAD)) || (cantidadesMenu != null && cantidadesMenu.stream().anyMatch(c -> c > MAX_CANTIDAD))) {

            redirectAttributes.addFlashAttribute("error", "La cantidad de cada producto debe ser entre 1 y " + MAX_CANTIDAD + " unidades.");
            return "redirect:/pedidos/editar-pedido-temporal";
        }

        // Asignamos la nueva cantidad a cada línea del pedido.
        for (int i = 0; i < numPlatos; i++) {
            platos.get(i).setCantidad(cantidadesPlato.get(i));
        }
        for (int i = 0; i < numMenus; i++) {
            menus.get(i).setCantidad(cantidadesMenu.get(i));
        }

        // Si el usuario ha eliminado alguna línea se pone la cantidad a 0
        if (eliminarPlato != null && eliminarPlato >= 0 && eliminarPlato < platos.size()) {
            platos.get(eliminarPlato).setCantidad(0);
        }
        if (eliminarMenu != null && eliminarMenu >= 0 && eliminarMenu < menus.size()) {
            menus.get(eliminarMenu).setCantidad(0);
        }

        // Limpia todas las líneas que han quedado a 0
        platos.removeIf(linea -> linea.getCantidad() < 1);
        menus.removeIf(linea -> linea.getCantidad() < 1);

        // Si el pedido temporal queda vacío redirige a carta e informa
        if (pedidoTemporal.estaVacio()) {
            redirectAttributes.addFlashAttribute("mensaje", "Has quitado todos los productos de tu pedido.");
            return "redirect:/carta";
        }

        // Al pulsar una X se queda en la pantalla de edición; con "Guardar y volver" vuelve a revisar
        if (eliminarPlato != null || eliminarMenu != null) {
            return "redirect:/pedidos/editar-pedido-temporal";
        }

        return "redirect:/pedidos/revisar";
    }

    @PostMapping("/confirmar")
    public String confirmarPedido(@RequestParam(required = false) String direccionEntrega, HttpSession session, Authentication authentication, RedirectAttributes redirectAttributes) {

        // Recuperamos el pedido temporal de la sesión
        PedidoTemporalDTO pedidoTemporal = PedidoTemporalDTO.deSesion(session);

        // Si el temporal de la sesión esta vacío se redirige a carta y se informa
        if (pedidoTemporal.estaVacio()) {
            redirectAttributes.addFlashAttribute("error", "Tu pedido está vacío. Añade platos de la carta o un menú del día.");
            return "redirect:/carta";
        }

        // Obtenemos los datos del usuario logueado
        Usuario usuario = usuarioService.buscarPorLogin(authentication.getName());

        Pedido pedidoConfirmado;

        try {

            // Guardar el pedido
            pedidoConfirmado = pedidoService.crearPedido(usuario.getIdUsuario(), direccionEntrega, pedidoTemporal.getLineasPlato(), pedidoTemporal.getLineasMenu());

        } catch (IllegalArgumentException | OperacionNoPermitidaException e) {

            // Se informa si hay un error
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            //Se guarda la direccion del usaurio apra que no tenga que volver a introducirla
            redirectAttributes.addFlashAttribute("direccionEntrega", direccionEntrega);

            return "redirect:/pedidos/revisar";
        }

        log.info("Pedido #{} confirmado por usuario='{}' ({} €)", pedidoConfirmado.getIdPedido(), authentication.getName(), pedidoConfirmado.getImporteTotal());

        // Se vacia el pedido temporal de la sesión
        pedidoTemporal.vaciar();

        // Se informa del exito en la confirmación del pedido
        redirectAttributes.addFlashAttribute("mensaje", "Pedido #" + pedidoConfirmado.getIdPedido() + " confirmado y registrado correctamente.");

        // Se redirige a los pedidos del usaurio para que pueda ver el pedido confirmado
        return "redirect:/pedidos/mis-pedidos";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos(Authentication authentication, Model model) {

        // Obtenemos los datos del usuario logueado
        Usuario usuario = usuarioService.buscarPorLogin(authentication.getName());

        // Obtenemos los pedidos realizados por el usuario
        List<Pedido> pedidos = pedidoService.listarPedidosDeUsuario(usuario.getIdUsuario());

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nombresEstado", NOMBRES_ESTADO);
        model.addAttribute("pasos", PASOS_SEGUIMIENTO);

        //Mapa con el resumen da cada pedido
        Map<Long, ResumenPedidoDTO> resumenesPedidos = new HashMap<>();

        for (Pedido pedido : pedidos) {
            resumenesPedidos.put(pedido.getIdPedido(), pedidoService.obtenerResumenDePedido(pedido.getIdPedido()));
        }

        model.addAttribute("resumenesPedidos", resumenesPedidos);

        return "pedidos/mis-pedidos";
    }


    @GetMapping("/{id}/modificar")
    public String mostrarEditarPedido(@PathVariable Long id) {
        // TODO (US-09): Cargar pedido y pasarlo a la vista.
        // TODO (P11): Crear HTML
        // P11: Modificar un pedido
        // Formulario para editar un pedido (solo dentro de los 10 min)
        return "pedidos/modificar";
    }


    @PostMapping("/{id}/modificar")
    public String guardarEdicionPedido(@PathVariable Long id) {
        // TODO (US-09): Llamar a PedidoService.modificarPedido(...).
        // Guardar los cambios del pedido
        return "redirect:/pedidos/mis-pedidos";
    }


    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id) {
        // TODO (US-10): Llamar a PedidoService.cancelarPedido(id).
        // Cancelar el pedido
        // TODO (P12): Crear HTML
        // P12: Pop up dentro de pantalla 10 para confirmar si se desea eliminar el pedido
        return "redirect:/pedidos/mis-pedidos";
    }
}
