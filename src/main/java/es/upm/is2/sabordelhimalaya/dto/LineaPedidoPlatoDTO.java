package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LineaPedidoPlatoDTO {

    @NotNull
    private Long idPlato;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
