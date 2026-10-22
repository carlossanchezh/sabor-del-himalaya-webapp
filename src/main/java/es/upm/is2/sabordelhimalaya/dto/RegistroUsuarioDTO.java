package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroUsuarioDTO {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 255)
    private String direccion;

    @NotBlank
    @Email
    @Size(max = 150)
    private String correo;

    @NotBlank
    @Size(min = 4, max = 50)
    private String login;

    @NotBlank
    @Size(min = 6, max = 100)
    private String contrasena;
}
