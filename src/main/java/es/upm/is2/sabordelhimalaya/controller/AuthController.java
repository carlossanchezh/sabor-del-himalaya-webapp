package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin() {
        // TODO (P1): Crear el HTML
        // P1: Muestra la pantalla única con el Login (izquierda) y el Registro (derecha)
        return "auth/login";
    }

    @PostMapping("/registro")
    public String procesarRegistro() {
        // TODO (US-01): Recibir DTO, validar, llamar a UsuarioService, enviar correo y redirigir a P2.
        return "redirect:/auth/registro-exitoso";
    }

    @GetMapping("/registro-exitoso")
    public String mostrarRegistroExitoso() {
        // TODO (P2): Crear el HTML
        // P2: Muestra la pantalla con la opción de reenviar correo e ir a iniciar sesión
        return "auth/cuenta-creada";
    }

    @GetMapping("/activar")
    public String activarCuenta(@RequestParam String token) {
        // TODO (US-02): Llamar a UsuarioService.activarCuenta(token) y redirigir.
        // TODO (P3): Crear el HTML
        // TODO (P4): Crear el HTML
        // P3: Muestra la pantalla con activación correcta la opción de ir a iniciar sesión
        // P4: Muestra la pantalla con activación fallida y la opción de reenviar correo
        return "auth/cuenta-activada";
        //return "auth/enlace-invalido"; si falla
    }

    @PostMapping("/reenviar")
    public String reenviarActivacion(@RequestParam String login) {
        // TODO (US-02): Llamar a UsuarioService.reenviarActivacion(login).
        return "redirect:/auth/registro-exitoso";
    }
}
