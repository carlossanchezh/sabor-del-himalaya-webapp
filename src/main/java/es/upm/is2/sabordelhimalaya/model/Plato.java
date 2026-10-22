package es.upm.is2.sabordelhimalaya.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;

import java.math.BigDecimal;

@Entity
@Table(name = "plato")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plato")
    private Long idPlato;

    @NotBlank
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @NotBlank
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @NotNull
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaPlato categoria;

    @NotNull
    @Column(name = "esta_en_carta", nullable = false)
    private Boolean estaEnCarta;

    @NotNull
    @Column(name = "es_del_chef", nullable = false)
    private Boolean esDelChef;
}
