package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.model.Pedido;

import es.upm.is2.sabordelhimalaya.dto.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.LineaPedidoPlatoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PedidoService {

    // US-06: Usuario crear un pedido y US-07: Usuario confirmar un pedido
    Pedido crearPedido(Long idUsuario, String direccionEntrega, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu);

    // US-08: Usuario ver el estado de sus pedidos
    List<Pedido> listarPedidosDeUsuario(Long idUsuario);

    // US-09: Usuario modificar pedido
    Pedido modificarPedido(Long idPedido, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu);

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
