package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LineaPedidoMenuDiarioDTO {

    @NotNull
    private Long idMenuDiario;

    @NotNull
    private Long idEntrante;

    @NotNull
    private Long idPrincipal;

    @NotNull
    private Long idPostre;

    @NotNull
    private Long idBebida;

    @NotNull
    @Min(1)
    private Integer cantidad;

}
