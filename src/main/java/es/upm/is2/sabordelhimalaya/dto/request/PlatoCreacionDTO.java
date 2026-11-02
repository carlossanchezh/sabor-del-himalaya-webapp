package es.upm.is2.sabordelhimalaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;

import java.math.BigDecimal;

@Data
public class PlatoCreacionDTO {
    
    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 255)
    private String descripcion;

    @NotNull
    @Positive
    private BigDecimal precio;

    @NotNull
    private CategoriaPlato categoria;
}
