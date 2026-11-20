package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.model.Pedido;

import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.dto.session.PedidoTemporalDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PedidoService {

    // US-06: Usuario crear un pedido y US-07: Usuario confirmar un pedido
    Pedido crearPedido(Long idUsuario, String direccionEntrega, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu);

    // US-06 / US-07: Calcular productos, subtotales e importe del carrito antes de confirmar
    ResumenPedidoDTO resumirPedidoTemporal(List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu);

    // US-08: Usuario ver el resumen de un pedido ya confirmado
    ResumenPedidoDTO obtenerResumenDePedido(Long idPedido);

    // US-08: Usuario ver el estado de sus pedidos
    List<Pedido> listarPedidosDeUsuario(Long idUsuario);

    // US-09 / US-10: Minutos tras la realización del pedido durante los que se puede modificar
    int MINUTOS_MODIFICACION = 10;

    // US-09: Usuario modificar pedido
    Pedido modificarPedido(Long idPedido, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu);

    // US-09: Comprueba si el pedido todavía se puede modificar (no cancelado y dentro de los 10 minutos)
    boolean sePuedeModificar(Pedido pedido);

    // US-09: Líneas de un pedido ya confirmado, en el mismo formato que el pedido temporal, para poder editarlas
    PedidoTemporalDTO obtenerLineasDePedido(Long idPedido);

    // US-10: Usuario cancelar pedido
    void cancelarPedido(Long idPedido);

    // US-11: Staff actualizar estado de un pedido
    Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado);

    // US-11: Staff consultar los pedidos del día
    List<Pedido> listarPedidosDelDia(LocalDate fecha);

    // US-19: Dueño consultar facturación diaria
    BigDecimal calcularFacturacion(LocalDate fecha);
    
    Pedido buscarPorId(Long id);
    List<Pedido> listarTodosPedidos();
}
