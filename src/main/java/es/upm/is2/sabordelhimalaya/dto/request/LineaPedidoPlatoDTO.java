package es.upm.is2.sabordelhimalaya.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class LineaPedidoPlatoDTO implements Serializable {

    @NotNull
    private Long idPlato;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
