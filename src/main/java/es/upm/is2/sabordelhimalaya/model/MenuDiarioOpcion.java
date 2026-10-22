package es.upm.is2.sabordelhimalaya.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;

@Entity
@Table(name = "menu_diario_opciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDiarioOpcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_menu_diario_opciones")
    private Long idMenuDiarioOpcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_menu_diario", nullable = false)
    private MenuDiario menuDiario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plato", nullable = false)
    private Plato plato;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaMenu categoria;
}