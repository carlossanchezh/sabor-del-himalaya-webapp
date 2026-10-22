package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.model.LineaPedidoMenuDiario;
import java.util.List;

@Repository
public interface LineaPedidoMenuDiarioRepository extends JpaRepository<LineaPedidoMenuDiario, Long> {

    // líneas de pedido de menu por pedido
    List<LineaPedidoMenuDiario> findByPedidoIdPedido(Long idPedido);
}
