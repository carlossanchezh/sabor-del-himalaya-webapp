package es.upm.is2.sabordelhimalaya.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import es.upm.is2.sabordelhimalaya.enums.Rol;


@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @NotBlank
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @NotBlank
    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @NotBlank
    @Email
    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @NotBlank
    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @NotBlank
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @NotNull
    @Column(name = "estado_activacion", nullable = false)
    private Boolean estadoActivacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Column(name = "token")
    private String tokenActivacion;
}
