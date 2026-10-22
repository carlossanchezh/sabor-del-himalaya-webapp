package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.model.Pedido;

import es.upm.is2.sabordelhimalaya.dto.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.LineaPedidoPlatoDTO;

import es.upm.is2.sabordelhimalaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlatoRepository platoRepository;
    private final MenuDiarioRepository menuDiarioRepository;
    private final LineaPedidoPlatoRepository lineaPedidoPlatoRepository;
    private final LineaPedidoMenuDiarioRepository lineaPedidoMenuDiarioRepository;

    @Override
    public Pedido crearPedido(Long idUsuario,String direccionEntrega, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {
        // TODO (US-06): Crear Pedido, calcular importe total, guardar líneas de plato y de menú.
        throw new UnsupportedOperationException("TODO US-06");
    }

    @Override
    public List<Pedido> listarPedidosDeUsuario(Long idUsuario) {
        // TODO (US-08): return pedidoRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(idUsuario);
        throw new UnsupportedOperationException("TODO US-08");
    }

    @Override
    public Pedido modificarPedido(Long idPedido, List<LineaPedidoPlatoDTO> lineasPlato, List<LineaPedidoMenuDiarioDTO> lineasMenu) {
        // TODO (US-09): Validar que no hayan pasado 10 min. Borrar líneas antiguas, crear nuevas, recalcular total.
        throw new UnsupportedOperationException("TODO US-09");
    }

    @Override
    public void cancelarPedido(Long idPedido) {
        // TODO (US-10): Validar que no hayan pasado 10 min. Cambiar estado a CANCELADO.
        throw new UnsupportedOperationException("TODO US-10");
    }

    @Override
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        // TODO (US-11): Buscar pedido, actualizar estado y guardar.
        throw new UnsupportedOperationException("TODO US-11");
    }

    @Override
    public List<Pedido> listarPedidosDelDia(LocalDate fecha) {
        // TODO (US-11): return pedidoRepository.findByFechaHoraBetween(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        throw new UnsupportedOperationException("TODO US-11");
    }

    @Override
    public BigDecimal calcularFacturacion(LocalDate fecha) {
        // TODO (US-19): Obtener pedidos del día y sumar importeTotal (excluyendo cancelados).
        throw new UnsupportedOperationException("TODO US-19");
    }

    @Override
    public Pedido buscarPorId(Long id) {
        // TODO: return pedidoRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Pedido> listarTodosPedidos() {
        // TODO: return pedidoRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }
}
