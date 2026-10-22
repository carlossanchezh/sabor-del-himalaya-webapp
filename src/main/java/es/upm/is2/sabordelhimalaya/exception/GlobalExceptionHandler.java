package es.upm.is2.sabordelhimalaya.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura el error 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Error 404
    public String usuarioNotFoundHandler(RecursoNoEncontradoException ex, Model model) {
        model.addAttribute("mensajeError", ex.getMessage()); // Pasamos el mensaje al HTML
        return "error/404"; // Devuelve la plantilla templates/error/404.html
    }

    // Captura el error 403
    @ExceptionHandler(OperacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // Error 403
    public String operacionNoPermitidaHandler(OperacionNoPermitidaException ex, Model model) {
        model.addAttribute("mensajeError", ex.getMessage());
        return "error/403"; // Devuelve la plantilla templates/error/403.html
    }
}
