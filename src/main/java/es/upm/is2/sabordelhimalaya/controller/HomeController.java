package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Redirige la raíz del proyecto a la carta principal
    @GetMapping("/")
    public String index() {
        // Si el usuario no está logueado, Spring Security interceptará esta ruta y lo mandará al login antes de que llegue aquí.
        // Si ya está logueado, le redirigimos a la carta.
        return "redirect:/carta";
    }
}