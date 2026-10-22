package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dueno")
public class DuenoController {

    @GetMapping("/panel")
    public String panel() {
        // TODO (US-15): Cargar la carta (platos) para mostrarla.
        // TODO (US-19): Calcular la facturación del día de hoy y pasarla al modelo.
        // TODO (P18): Crear HTML "dueno/panel"
        // P18: Panel de gestión (Ver Carta + Ver Facturación del día) botones de editar añadir y eliminar platos
        return "dueno/panel";
    }

    @PostMapping("/platos/nuevo")
    public String guardarNuevoPlato() {
        // TODO (US-17): Recibir datos, llamar a PlatoService.anadirPlatoALaCarta().
        // Guardar el plato nuevo en la carta
        // TODO (P20/P21): Crear HTML
        // P20/P21: Formulario para añadir un plato completamente nuevo (es un pop up dentro de dueno/panel)
        return "redirect:/dueno/panel";
    }


    @PostMapping("/platos/{id}/anadir-existente")
    public String anadirPlatoExistente(@PathVariable Long id) {
        // TODO (US-17): Llamar a PlatoService.anadirPlatoExistenteALaCarta(id).
        // P20: Añadir a la carta un plato que ya existía en el sistema
        return "redirect:/dueno/panel";
    }

    @PostMapping("/platos/{id}/editar")
    public String guardarEdicionPlato(@PathVariable Long id) {
        // TODO (US-16): Recibir datos, llamar a PlatoService.modificarPlato().
        // Guardar los cambios del plato editado
        // TODO (P19): Crear HTML
        // P19: Formulario para editar un plato existente (es un pop up dentro de dueno/panel)
        return "redirect:/dueno/panel";
    }


    @PostMapping("/platos/{id}/eliminar")
    public String eliminarPlato(@PathVariable Long id) {
        // TODO (US-18): Llamar a PlatoService.retirarPlatoDeLaCarta(id).
        // Eliminar (retirar) plato de la carta (Borrado lógico)
        // TODO (P22): Crear HTML
        // P22: pop up de confirmar eliminación dentro de dueno/panel
        return "redirect:/dueno/panel";
    }
}