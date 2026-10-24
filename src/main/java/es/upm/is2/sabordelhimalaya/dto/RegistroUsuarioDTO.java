package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroUsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 120, message = "El nombre es demasiado largo.")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(max = 255, message = "La dirección es demasiado larga.")
    private String direccion;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El formato del correo no es válido.")
    @Size(max = 150, message = "El correo es demasiado largo.")
    private String correo;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres.")
    private String login;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres.")
    private String contrasena;
}
