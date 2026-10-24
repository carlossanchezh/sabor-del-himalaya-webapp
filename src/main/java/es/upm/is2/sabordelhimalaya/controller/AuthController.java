package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;

import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {

        // Crear DTO si no existe
        if (!model.containsAttribute("registroUsuarioDTO")) {
            model.addAttribute("registroUsuarioDTO", new RegistroUsuarioDTO());
        }
        return "auth/login";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute RegistroUsuarioDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {

        // Si faltan datos o hay errores de formato
        if (result.hasErrors()) {
            return "auth/login";
        }

        try {

            // Registrar al usuario
            usuarioService.registrar(dto);

            // Pasamos el login y el correo para usarlos en la pantalla de registro exitoso
            redirectAttributes.addFlashAttribute("login", dto.getLogin());
            redirectAttributes.addFlashAttribute("correo", dto.getCorreo());

            // Redirección a la pantalla de registro exitoso
            return "redirect:/auth/registro-exitoso";

        } catch (OperacionNoPermitidaException e) {

            // Manejo de posibles errores (correo, login ya utilizados)
            if (e.getMessage().toLowerCase().contains("correo")) {

                result.rejectValue("correo", "error.correo", e.getMessage());

            } else if (e.getMessage().toLowerCase().contains("login")) {

                result.rejectValue("login", "error.login", e.getMessage());

            } else {

                result.reject("error.global", "Ha ocurrido un error inesperado.");

            }

            return "auth/login";

        }
    }

    @GetMapping("/registro-exitoso")
    public String mostrarRegistroExitoso() {

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
