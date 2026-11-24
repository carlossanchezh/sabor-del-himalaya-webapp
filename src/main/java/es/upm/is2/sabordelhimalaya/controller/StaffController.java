package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.request.CambioEstadoDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final PedidoService pedidoService;

    // Textos asociados a cada estado del pedido
    private static final Map<String, String> NOMBRES_ESTADO = new LinkedHashMap<>();
    static {
        NOMBRES_ESTADO.put(EstadoPedido.RECIBIDO.name(), "Recibido");
        NOMBRES_ESTADO.put(EstadoPedido.EN_PREPARACION.name(), "En preparación");
        NOMBRES_ESTADO.put(EstadoPedido.EN_REPARTO.name(), "En reparto");
        NOMBRES_ESTADO.put(EstadoPedido.ENTREGADO.name(), "Entregado");
        NOMBRES_ESTADO.put(EstadoPedido.CANCELADO.name(), "Cancelado");
    }

    @GetMapping("/pedidos")
    public String verPedidosDelDia(@RequestParam(defaultValue = "todos") String tabActiva, Model model) {

        LocalDate hoy = LocalDate.now();

        // Pedidos de hoy, del más reciente al más antiguo
        List<Pedido> pedidos = pedidoService.listarPedidosDelDia(hoy).stream()
                .sorted(Comparator.comparing(Pedido::getFechaHora).reversed())
                .toList();

        // Lista de pestañas: "Todos" + una por cada estado
        List<Map<String, Object>> pestanas = new ArrayList<>();
        pestanas.add(Map.of("id", "todos", "etiqueta", "Todos", "pedidos", pedidos));

        for (EstadoPedido estado : EstadoPedido.values()) {
            List<Pedido> filtrados = pedidos.stream().filter(p -> p.getEstado() == estado).toList();
            pestanas.add(Map.of("id", estado.name(), "etiqueta", NOMBRES_ESTADO.get(estado.name()), "pedidos", filtrados));
        }

        model.addAttribute("fecha", hoy);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("nombresEstado", NOMBRES_ESTADO);
        model.addAttribute("pestanas", pestanas);

        model.addAttribute("tabActiva", tabActiva);

        // Resumen de productos de cada pedido
        Map<Long, ResumenPedidoDTO> resumenesPedidos = new HashMap<>();
        for (Pedido pedido : pedidos) {
            resumenesPedidos.put(pedido.getIdPedido(), pedidoService.obtenerResumenDePedido(pedido.getIdPedido()));
        }
        model.addAttribute("resumenesPedidos", resumenesPedidos);

        return "staff/pedidos";
    }

    @PostMapping("/pedidos/estado")
    public String cambiarEstadoPedido(@Valid @ModelAttribute CambioEstadoDTO cambioEstadoDTO, BindingResult bindingResult, @RequestParam(defaultValue = "todos") String tabActiva, RedirectAttributes redirectAttributes, Authentication authentication) {

        redirectAttributes.addAttribute("tabActiva", tabActiva);

        // Datos incompletos o estado no válido
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un estado válido para el pedido.");
            return "redirect:/staff/pedidos";
        }

        try {

            // Se actualiza el estado del pedido
            Pedido pedido = pedidoService.cambiarEstado(cambioEstadoDTO.getIdPedido(), cambioEstadoDTO.getNuevoEstado());

            log.info("Staff '{}' ha cambiado el estado del pedido #{} a {}", authentication.getName(), pedido.getIdPedido(), pedido.getEstado());

            // Saltamos a la pestaña del nuevo estado (si está en todos se queda ahí)
            if (!"todos".equals(tabActiva)) {
                redirectAttributes.addAttribute("tabActiva", pedido.getEstado().name());
            }

        } catch (RecursoNoEncontradoException e) {

            // Pedido inexistente
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Tras pulsar actualizar, recargamos la misma página para ver el cambio reflejado
        return "redirect:/staff/pedidos";
    }
}
