package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

@Controller
@RequestMapping("/dueno")
@RequiredArgsConstructor 
public class DuenoController {

    private final PlatoService platoService;

    @GetMapping("/panel")
    public String panel(Model model) {
        // TODO (US-15): Cargar la carta (platos) para mostrarla.
        // TODO (US-19): Calcular la facturación del día de hoy y pasarla al modelo.
        // TODO (P18): Crear HTML "dueno/panel"
        // P18: Panel de gestión (Ver Carta + Ver Facturación del día) botones de editar añadir y eliminar platos

        //Formulario vacio para el popup de añadir plato
        model.addAttribute("nuevoPlato", new PlatoCreacionDTO());
        cargarDatosPanel(model);
        return "dueno/panel";
    }

    @PostMapping("/platos/nuevo")
    public String guardarNuevoPlato(@Valid @ModelAttribute("nuevoPlato") PlatoCreacionDTO nuevoPlato,
                                    BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // TODO (US-17): Recibir datos, llamar a PlatoService.anadirPlatoALaCarta().
        // Guardar el plato nuevo en la carta
        // TODO (P20/P21): Crear HTML
        // P20/P21: Formulario para añadir un plato completamente nuevo (es un pop up dentro de dueno/panel)
        if(bindingResult.hasErrors()){
            //Volvemos al panel con el popup abierto y los errores de cada campo
            model.addAttribute("mostrarModal", true);
            cargarDatosPanel(model);
            return "dueno/panel";
        }
        try {
            platoService.anadirPlatoALaCarta(nuevoPlato);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("mostrarModal", true);
            cargarDatosPanel(model);
            return "dueno/panel";
        }
        redirectAttributes.addFlashAttribute("mensaje", "Plato '" + nuevoPlato.getNombre().trim() + "' añadido a la carta.");
        return "redirect:/dueno/panel";
    }


    @PostMapping("/platos/{id}/anadir-existente")
    public String anadirPlatoExistente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // TODO (US-17): Llamar a PlatoService.anadirPlatoExistenteALaCarta(id).
        // P20: Añadir a la carta un plato que ya existía en el sistema
        try {
            Plato plato = platoService.anadirPlatoExistenteALaCarta(id);
            redirectAttributes.addFlashAttribute("mensaje", "Plato '" + plato.getNombre() + "' añadido de nuevo a la carta.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorCarta", e.getMessage());
        }
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

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    //carta ordenada ppor categoria(entrante, principal, postre y bebida) y por nombre
    private void cargarDatosPanel(Model model){
        model.addAttribute("platos", platoService.listarCarta().stream()
        .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre)).toList());
        model.addAttribute("categorias", CategoriaPlato.values());
        model.addAttribute("platosRetirados", platoService.listarPlatosRetiradosDeLaCarta().stream()
        .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre)).toList());
    }
}