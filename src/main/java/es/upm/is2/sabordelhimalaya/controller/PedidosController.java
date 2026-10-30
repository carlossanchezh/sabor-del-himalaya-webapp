package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.Carrito;
import es.upm.is2.sabordelhimalaya.dto.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.service.PedidoService;
import es.upm.is2.sabordelhimalaya.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidosController {

    // Textos de cada estado para la pantalla "Mis pedidos" (CA8.2)
    private static final Map<EstadoPedido, String> NOMBRES_ESTADO = new LinkedHashMap<>();
    static {
        NOMBRES_ESTADO.put(EstadoPedido.RECIBIDO, "Recibido");
        NOMBRES_ESTADO.put(EstadoPedido.EN_PREPARACION, "En preparación");
        NOMBRES_ESTADO.put(EstadoPedido.EN_REPARTO, "En reparto");
        NOMBRES_ESTADO.put(EstadoPedido.ENTREGADO, "Entregado");
        NOMBRES_ESTADO.put(EstadoPedido.CANCELADO, "Cancelado");
    }

    // Pasos de la barra de progreso de cada pedido (P10)
    private static final List<EstadoPedido> PASOS_SEGUIMIENTO = List.of(
            EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO);

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    // US-06 (CA6.3, CA6.4): Añadir un plato de la carta (o una bebida/postre extra) al carrito
    @PostMapping("/anadir-plato")
    public String anadirPlato(@RequestParam Long idPlato, @RequestParam(defaultValue = "1") Integer cantidad,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        LineaPedidoPlatoDTO linea = new LineaPedidoPlatoDTO();
        linea.setIdPlato(idPlato);
        linea.setCantidad(cantidad);

        try {
            // Comprueba que el plato existe, está en carta y la cantidad es válida antes de meterlo en el carrito
            pedidoService.resumirCarrito(List.of(linea), List.of());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carta";
        }

        Carrito.deSesion(session).anadirPlato(linea);
        return "redirect:/carta";
    }

    // US-06 (CA6.1, CA6.2): Añadir un menú diario con el entrante, principal, postre y bebida elegidos
    @PostMapping("/anadir-menu")
    public String anadirMenu(@Valid @ModelAttribute LineaPedidoMenuDiarioDTO linea, BindingResult bindingResult,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    "Para añadir el menú del día elige un entrante, un principal, un postre y una bebida.");
            return "redirect:/carta";
        }

        try {
            // Comprueba que los platos elegidos son opciones del menú de hoy
            pedidoService.resumirCarrito(List.of(), List.of(linea));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carta";
        }

        Carrito.deSesion(session).anadirMenu(linea);
        return "redirect:/carta";
    }

    // US-07 / P8: Resumen del pedido con subtotales, total y dirección de entrega antes de confirmar
    // (el resumen del carrito "resumenCarrito" lo añade CarritoModelAdvice)
    @GetMapping("/revisar")
    public String revisarPedido(HttpSession session, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (Carrito.deSesion(session).estaVacio()) {
            return redirigirCarritoVacio(model, redirectAttributes);
        }

        Usuario usuario = usuarioService.buscarPorLogin(principal.getName());
        model.addAttribute("usuario", usuario);
        // Si vuelve de un error al confirmar se mantiene la dirección que había escrito
        if (!model.containsAttribute("direccionEntrega")) {
            model.addAttribute("direccionEntrega", usuario.getDireccion());
        }
        return "pedidos/revisar";
    }

    // P9: Editar cantidades o quitar productos del carrito ANTES de confirmar
    @GetMapping("/editar-carrito")
    public String mostrarEditarCarrito(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (Carrito.deSesion(session).estaVacio()) {
            return redirigirCarritoVacio(model, redirectAttributes);
        }
        return "pedidos/editar-carrito";
    }

    // P9: Guardar las nuevas cantidades (0 = quitar) y quitar la línea pulsada con la X
    @PostMapping("/editar-carrito")
    public String guardarCambiosCarrito(@RequestParam(required = false) List<Integer> cantidadesPlato,
                                        @RequestParam(required = false) List<Integer> cantidadesMenu,
                                        @RequestParam(required = false) Integer eliminarPlato,
                                        @RequestParam(required = false) Integer eliminarMenu,
                                        HttpSession session, RedirectAttributes redirectAttributes) {
        Carrito carrito = Carrito.deSesion(session);
        List<LineaPedidoPlatoDTO> platos = carrito.getLineasPlato();
        List<LineaPedidoMenuDiarioDTO> menus = carrito.getLineasMenu();

        int numPlatos = (cantidadesPlato != null) ? cantidadesPlato.size() : 0;
        int numMenus = (cantidadesMenu != null) ? cantidadesMenu.size() : 0;
        if (numPlatos != platos.size() || numMenus != menus.size()
                || (cantidadesPlato != null && cantidadesPlato.contains(null))
                || (cantidadesMenu != null && cantidadesMenu.contains(null))) {
            // El carrito ha cambiado desde que se abrió la pantalla (p. ej. en otra pestaña) o falta alguna cantidad
            redirectAttributes.addFlashAttribute("error", "No se han podido guardar los cambios. Revisa las cantidades e inténtalo de nuevo.");
            return "redirect:/pedidos/editar-carrito";
        }

        for (int i = 0; i < numPlatos; i++) {
            platos.get(i).setCantidad(cantidadesPlato.get(i));
        }
        for (int i = 0; i < numMenus; i++) {
            menus.get(i).setCantidad(cantidadesMenu.get(i));
        }
        if (eliminarPlato != null && eliminarPlato >= 0 && eliminarPlato < platos.size()) {
            platos.get(eliminarPlato).setCantidad(0);
        }
        if (eliminarMenu != null && eliminarMenu >= 0 && eliminarMenu < menus.size()) {
            menus.get(eliminarMenu).setCantidad(0);
        }
        platos.removeIf(linea -> linea.getCantidad() < 1);
        menus.removeIf(linea -> linea.getCantidad() < 1);

        if (carrito.estaVacio()) {
            redirectAttributes.addFlashAttribute("mensaje", "Has quitado todos los productos de tu pedido.");
            return "redirect:/carta";
        }
        // Al pulsar una X se queda en la pantalla de edición; con "Guardar y volver" vuelve a revisar
        if (eliminarPlato != null || eliminarMenu != null) {
            return "redirect:/pedidos/editar-carrito";
        }
        return "redirect:/pedidos/revisar";
    }

    // US-07 (CA7.3): Confirmar y registrar el pedido en la Base de Datos
    @PostMapping("/confirmar")
    public String confirmarPedido(@RequestParam(required = false) String direccionEntrega, HttpSession session,
                                  Principal principal, RedirectAttributes redirectAttributes) {
        Carrito carrito = Carrito.deSesion(session);
        if (carrito.estaVacio()) {
            redirectAttributes.addFlashAttribute("error", "Tu pedido está vacío. Añade platos de la carta o un menú del día.");
            return "redirect:/carta";
        }

        Usuario usuario = usuarioService.buscarPorLogin(principal.getName());
        Pedido pedido;
        try {
            pedido = pedidoService.crearPedido(usuario.getIdUsuario(), direccionEntrega,
                    carrito.getLineasPlato(), carrito.getLineasMenu());
        } catch (IllegalArgumentException | OperacionNoPermitidaException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("direccionEntrega", direccionEntrega);
            return "redirect:/pedidos/revisar";
        }

        carrito.vaciar();
        redirectAttributes.addFlashAttribute("mensaje",
                "Pedido #" + pedido.getIdPedido() + " confirmado y registrado correctamente.");
        return "redirect:/pedidos/mis-pedidos";
    }

    // US-08 / P10: Pedidos del usuario logueado con su estado actual
    @GetMapping("/mis-pedidos")
    public String misPedidos(Principal principal, Model model) {
        Usuario usuario = usuarioService.buscarPorLogin(principal.getName());
        model.addAttribute("pedidos", pedidoService.listarPedidosDeUsuario(usuario.getIdUsuario()));
        model.addAttribute("nombresEstado", NOMBRES_ESTADO);
        model.addAttribute("pasos", PASOS_SEGUIMIENTO);
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

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    // Vuelve a la carta avisando de que el carrito está vacío (o de por qué CarritoModelAdvice lo ha vaciado)
    private String redirigirCarritoVacio(Model model, RedirectAttributes redirectAttributes) {
        Object errorCarrito = model.getAttribute("errorCarrito");
        redirectAttributes.addFlashAttribute("error", (errorCarrito != null) ? errorCarrito
                : "Tu pedido está vacío. Añade platos de la carta o un menú del día.");
        return "redirect:/carta";
    }
}
