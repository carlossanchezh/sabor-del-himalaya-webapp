package es.upm.is2.sabordelhimalaya.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class LineaPedidoMenuDiarioDTO implements Serializable {

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
    @Max(value = 99)
    private Integer cantidad;

}
