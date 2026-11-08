package es.upm.is2.sabordelhimalaya.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura el error 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Error 404
    public String recursoNotFoundHandler(RecursoNoEncontradoException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("mensajeError", ex.getMessage()); // Pasamos el mensaje al HTML
        return "error/404"; // Devuelve la plantilla templates/error/404.html
    }

    // Captura el error 403
    @ExceptionHandler(OperacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // Error 403
    public String operacionNoPermitidaHandler(OperacionNoPermitidaException ex, Model model) {
        log.warn("Operación no permitida: {}", ex.getMessage());
        model.addAttribute("mensajeError", ex.getMessage());
        return "error/403"; // Devuelve la plantilla templates/error/403.html
    }

    // Captura el error 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Error 500
    public String handleGenericException(Exception ex, Model model) {
        log.error("Error inesperado no controlado", ex);
        model.addAttribute("mensajeError", "Ha ocurrido un error inesperado. Por favor, inténtalo más tarde.");
        return "error/500"; // Devuelve la plantilla templates/error/500.html
    }
}
