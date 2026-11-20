package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.request.CambioEstadoDTO;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final PedidoService pedidoService;

    // Textos asociados a cada estado del pedido
    private static final Map<EstadoPedido, String> NOMBRES_ESTADO = new LinkedHashMap<>();
    static {
        NOMBRES_ESTADO.put(EstadoPedido.RECIBIDO, "Recibido");
        NOMBRES_ESTADO.put(EstadoPedido.EN_PREPARACION, "En preparación");
        NOMBRES_ESTADO.put(EstadoPedido.EN_REPARTO, "En reparto");
        NOMBRES_ESTADO.put(EstadoPedido.ENTREGADO, "Entregado");
        NOMBRES_ESTADO.put(EstadoPedido.CANCELADO, "Cancelado");
    }

    // P13: Panel de control para ver los pedidos del día (con pestañas por estado) y actualizarlos
    @GetMapping("/pedidos")
    public String verPedidosDelDia(Model model) {

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
            pestanas.add(Map.of("id", estado.name(), "etiqueta", NOMBRES_ESTADO.get(estado), "pedidos", filtrados));
        }

        model.addAttribute("fecha", hoy);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("nombresEstado", NOMBRES_ESTADO);
        model.addAttribute("pestanas", pestanas);

        return "staff/pedidos";
    }

    // P13: Cambio de estado desde el desplegable de la tabla de pedidos
    @PostMapping("/pedidos/estado")
    public String cambiarEstadoPedido(@Valid @ModelAttribute CambioEstadoDTO cambioEstadoDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // Datos incompletos o estado no válido
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un estado válido para el pedido.");
            return "redirect:/staff/pedidos";
        }

        try {

            // Se actualiza el estado del pedido
            Pedido pedido = pedidoService.cambiarEstado(cambioEstadoDTO.getIdPedido(), cambioEstadoDTO.getNuevoEstado());

            redirectAttributes.addFlashAttribute("mensaje", "Pedido #" + pedido.getIdPedido() + " actualizado a '" + NOMBRES_ESTADO.get(pedido.getEstado()) + "'.");

        } catch (RecursoNoEncontradoException e) {

            // Pedido inexistente
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Tras pulsar actualizar, recargamos la misma página para ver el cambio reflejado
        return "redirect:/staff/pedidos";
    }
}
