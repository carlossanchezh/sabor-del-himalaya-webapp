package es.upm.is2.sabordelhimalaya.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "menu_diario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_menu_diario")
    private Long idMenuDiario;

    @NotNull
    @Column(name = "fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @NotNull
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;
}
