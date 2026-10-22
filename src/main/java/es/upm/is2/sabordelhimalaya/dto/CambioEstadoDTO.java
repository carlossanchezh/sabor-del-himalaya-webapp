package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;

@Data
public class CambioEstadoDTO {

    @NotNull
    private Long idPedido;

    @NotNull
    private EstadoPedido nuevoEstado;
}
