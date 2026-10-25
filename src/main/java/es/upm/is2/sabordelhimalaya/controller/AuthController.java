package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;

import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
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

        boolean exito = usuarioService.activarCuenta(token);

        if (exito) {

            return "auth/cuenta-activada";
        } else {

            return "auth/enlace-invalido";
        }
    }

    @PostMapping("/reenviar")
    public String reenviarActivacion(@RequestParam String logincorreo, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        try {

            usuarioService.reenviarActivacion(logincorreo);
            redirectAttributes.addFlashAttribute("mensajeExito", "Se ha enviado un nuevo enlace de activación a tu correo.");

        } catch (OperacionNoPermitidaException e) {

            // Si la cuenta ya está activada, mostramos un error
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (RecursoNoEncontradoException e) {

            // Si el usuario o el correo no existen en la BD, mostramos error
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());

        } catch (Exception e) {

            // Si hay un fallo externo o desconocido
            redirectAttributes.addFlashAttribute("mensajeError", "No se ha podido enviar el correo. Inténtalo más tarde.");
        }

        // Obtenemos la URL desde la que se hizo la petición
        String referer = request.getHeader("Referer");

        // Volvemos a pasar los datos a la pantalla
        redirectAttributes.addFlashAttribute("login", logincorreo);
        redirectAttributes.addFlashAttribute("correo", logincorreo);

        // Si la URL es válida y pertenece a nuestra app, volvemos a ella. Si no, al login.
        if (referer != null && referer.contains("/auth/")) {
            // Extraemos solo la ruta
            String ruta = referer.substring(referer.indexOf("/auth/"));
            return "redirect:" + ruta;
        }

        // Por defecto, volvemos al login
        return "redirect:/auth/login";

    }
}
