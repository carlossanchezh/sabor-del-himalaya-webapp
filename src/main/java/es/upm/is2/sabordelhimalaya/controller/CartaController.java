package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/carta")
public class CartaController {

    @GetMapping
    public String mostrarCarta() {
        // TODO (US-04): Obtener platos de PlatoService y pasarlos a la vista.
        // TODO (P5): Crear el HTML
        // TODO (P6): Crear el HTML
        // P5: Vista de la carta para usuario (una vista de entrantes otra principales otra postres otra bebidas)
        // P6: Vista del menu del dia y sus opciones como vista en la misma pantalla
        // P5 y P6 en un unico html que maneja las vistas
        return "carta/index";
    }

}