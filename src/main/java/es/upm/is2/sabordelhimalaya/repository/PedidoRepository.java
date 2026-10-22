package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // pedidos de un usuario de más reciente a más antiguo
    List<Pedido> findByUsuarioIdUsuarioOrderByFechaHoraDesc(Long idUsuario);

    // pedidos en un determinado estado
    List<Pedido> findByEstado(EstadoPedido estado);

    // pedidos entre dos fechas
    List<Pedido> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

}
