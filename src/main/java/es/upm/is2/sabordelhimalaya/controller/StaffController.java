package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @GetMapping("/pedidos")
    public String verPedidosDelDia() {
        // TODO (US-11): Obtener la lista de pedidos de hoy (LocalDate.now()) y pasarla al modelo.
        // TODO (P13): Crear el HTML
        // P13: Panel de control para ver los pedidos del día (con las pestañas: Todos, Recibido, En preparación, etc.) y botón actualizar
        return "staff/pedidos";
    }


    @PostMapping("/pedidos/estado")
    public String cambiarEstadoPedido() {
        // TODO (US-11): Recibir el ID del pedido y el nuevo EstadoPedido.
        // TODO: Llamar a PedidoService.cambiarEstado(idPedido, nuevoEstado).
        // Procesar el cambio de estado desde el desplegable de la P13
        // Tras pulsar actualizar, recargamos la misma página para ver el cambio reflejado.
        return "redirect:/staff/pedidos";
    }
}