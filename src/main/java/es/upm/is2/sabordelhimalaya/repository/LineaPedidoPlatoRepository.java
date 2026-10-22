package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.model.LineaPedidoPlato;

import java.util.List;

@Repository
public interface LineaPedidoPlatoRepository extends JpaRepository<LineaPedidoPlato, Long> {

    // líneas de pedido de platos de un pedido
    List<LineaPedidoPlato> findByPedidoIdPedido(Long idPedido);
}
