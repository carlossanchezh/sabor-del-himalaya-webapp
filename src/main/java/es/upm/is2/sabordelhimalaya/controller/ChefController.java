package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chef")
public class ChefController {

    @GetMapping("/carta")
    public String verCarta() {
        // TODO (US-12): Obtener platos y pasarlos a la vista.
        // TODO (P14): Crear HTML
        // P14: Panel de la cocina (Consultar la carta del restaurante) y botones de crar plato y configurar menu (si no esta configurado)
        return "chef/carta";
    }

    @GetMapping("/menu")
    public String configurarMenu() {
        // TODO (US-13): Cargar listas de platos (de carta y fuera de carta) para los desplegables.
        // TODO (P15/P16): Crear HTML
        // P15/P16: Formulario para configurar el menú diario (con los desplegables)
        return "chef/menu";
    }


    @PostMapping("/menu")
    public String guardarMenu() {
        // TODO (US-13): Recibir datos y llamar a MenuDiarioService.configurarMenu().
        // Guardar el menú configurado y publicarlo
        return "redirect:/chef/carta";
    }

    @GetMapping("/platos/nuevo")
    public String mostrarNuevoPlato() {
        // TODO (P17): Crear HTML "chef/nuevo-plato"
        // P17: Formulario para crear un plato fuera de carta (para menús futuros)
        // En chef/nuevo-plato.html añadir campo oculto <input type="hidden" name="precio" value="0"> para que no falle
        return "chef/nuevo-plato";
    }

    @PostMapping("/platos/nuevo")
    public String guardarPlatoFueraDeCarta() {
        // TODO (US-14): Recibir datos y llamar a PlatoService.crearPlatoFueraDeCarta().
        // Guardar el plato fuera de carta en la base de datos
        return "redirect:/chef/carta";
    }
}