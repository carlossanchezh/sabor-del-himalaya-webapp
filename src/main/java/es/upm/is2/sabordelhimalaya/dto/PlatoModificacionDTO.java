package es.upm.is2.sabordelhimalaya.dto;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private BigDecimal precio;

    @NotNull
    private CategoriaPlato categoria;
}
