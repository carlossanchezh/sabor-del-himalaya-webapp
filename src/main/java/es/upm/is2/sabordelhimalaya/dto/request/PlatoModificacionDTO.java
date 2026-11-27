package es.upm.is2.sabordelhimalaya.dto.request;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlatoModificacionDTO {

    @NotNull
    private Long idPlato; //solo para modificar plato (si lo añade el dueño o lo crea el chef va vacío)

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 255)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 6, fraction = 2) 
    private BigDecimal precio;

    @NotNull
    private CategoriaPlato categoria;
}
