package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
public class PedidosController {

    @PostMapping("/anadir-plato")
    public String anadirPlato() {
        // TODO (US-06): Recibir datos, añadir a la sesión temporal.
        // Añadir un plato de la carta al pedido (carrito)
        return "redirect:/carta";
    }

    @PostMapping("/anadir-menu")
    public String anadirMenu() {
        // TODO (US-06): Recibir datos, añadir a la sesión temporal.
        // Añadir un menú diario al pedido (carrito)
        return "redirect:/carta";
    }


    @GetMapping("/revisar")
    public String revisarPedido() {
        // TODO (US-07): Leer la sesión y mostrar el resumen del pedido.
        // TODO (P8): Crear HTML
        // P8: Pantalla para revisar el pedido antes de confirmar
        return "pedidos/revisar";
    }

    @GetMapping("/editar-carrito")
    public String mostrarEditarCarrito() {
        // TODO (P9): Leer la sesión y pasarla a la vista.
        // TODO (P9): Crear HTML
        // P9: Mostrar pantalla para editar el carrito ANTES de confirmar
        return "pedidos/editar-carrito";
    }

    @PostMapping("/editar-carrito")
    public String guardarCambiosCarrito() {
        // TODO (US-09/P9): Recibir los datos modificados, actualizar la sesión.
        // Guardar cambios del carrito
        return "redirect:/pedidos/revisar";
    }

    @PostMapping("/confirmar")
    public String confirmarPedido() {
        // TODO (US-07): Llamar a PedidoService.crearPedido(...) y vaciar la sesión.
        // Confirmar y registrar el pedido en la Base de Datos
        return "redirect:/pedidos/mis-pedidos";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos() {
        // TODO (US-08): Buscar pedidos del usuario y pasarlos a la vista.
        // TODO (P10): Crear HTML
        // P10: Pantalla con todos los pedidos del usaurio y botnoes de eliminar y modificar
        // Listar los pedidos del usuario logueado
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