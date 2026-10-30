package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.Carrito;
import es.upm.is2.sabordelhimalaya.dto.ResumenCarritoDTO;
import es.upm.is2.sabordelhimalaya.service.PedidoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// US-06 (CA6.5): Añade el resumen del carrito ("resumenCarrito") a las vistas de la carta y de pedidos,
// para pintar el panel "Mi pedido" con el fragmento pedidos/fragmentos :: miPedido
@ControllerAdvice(assignableTypes = {CartaController.class, PedidosController.class})
@RequiredArgsConstructor
public class CarritoModelAdvice {

    private final PedidoService pedidoService;

    @ModelAttribute("resumenCarrito")
    public ResumenCarritoDTO resumenCarrito(HttpSession session, Model model) {
        Carrito carrito = Carrito.deSesion(session);
        if (carrito.estaVacio()) {
            return ResumenCarritoDTO.vacio();
        }
        try {
            return pedidoService.resumirCarrito(carrito.getLineasPlato(), carrito.getLineasMenu());
        } catch (IllegalArgumentException e) {
            // Algún producto ha dejado de estar disponible (retirado de la carta, menú de otro día...)
            carrito.vaciar();
            model.addAttribute("errorCarrito", e.getMessage() + " Se ha vaciado tu pedido, vuelve a añadir los productos.");
            return ResumenCarritoDTO.vacio();
        }
    }
}
