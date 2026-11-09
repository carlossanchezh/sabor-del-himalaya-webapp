package es.upm.is2.sabordelhimalaya.advice;

import es.upm.is2.sabordelhimalaya.controller.CartaController;
import es.upm.is2.sabordelhimalaya.controller.PedidosController;
import es.upm.is2.sabordelhimalaya.dto.session.PedidoTemporalDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.service.PedidoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {CartaController.class, PedidosController.class})
@RequiredArgsConstructor
public class PedidoTemporalModelAdvice {

    private final PedidoService pedidoService;

    @ModelAttribute("resumenPedidoTemporal")
    public ResumenPedidoDTO calcularResumenPedido(HttpSession session, Model model) {

        // Recuperamos el pedido de la sesión
        PedidoTemporalDTO pedidoTemporal = PedidoTemporalDTO.deSesion(session);

        // Si está vacío devuelve un pedido temporal vacío
        if (pedidoTemporal.estaVacio()) {
            return ResumenPedidoDTO.vacio();
        }

        try {

            return pedidoService.resumirPedidoTemporal(pedidoTemporal.getLineasPlato(), pedidoTemporal.getLineasMenu());

        } catch (IllegalArgumentException e) {

            // Algún producto ha dejado de estar disponible
            pedidoTemporal.vaciar();
            model.addAttribute("errorPedidoTemporal", e.getMessage() + " Se ha vaciado tu pedido, vuelve a añadir los productos.");
            return ResumenPedidoDTO.vacio();
        }
    }
}
