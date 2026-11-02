package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.request.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
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
        // TODO (US-19): Calcular la facturación del día de hoy y pasarla al modelo.

        // Formulario vacío para añadir paltos nuevos
        model.addAttribute("nuevoPlato", new PlatoCreacionDTO());

        // Carga los platos del sistema
        cargarCarta(model);

        return "dueno/panel";
    }

    @PostMapping("/platos/nuevo")
    public String guardarNuevoPlato(@Valid @ModelAttribute("nuevoPlato") PlatoCreacionDTO nuevoPlato, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        // Validación del formulario
        if(bindingResult.hasErrors()){

            // Informa de los errores y vuelve a abrir el modal
            model.addAttribute("mostrarModal", true);

            // Carga los platos del sistema
            cargarCarta(model);

            return "dueno/panel";
        }

        try {

            // Añadir plato nuevo a la carta
            platoService.anadirPlatoALaCarta(nuevoPlato);

        } catch (IllegalArgumentException e) {

            // Manejo de excepciones y muestra el modal nuevamente
            model.addAttribute("error", e.getMessage());
            model.addAttribute("mostrarModal", true);

            // Carga los platos del sistema
            cargarCarta(model);

            return "dueno/panel";
        }

        // Mensaje de éxito del plato añadido
        redirectAttributes.addFlashAttribute("mensaje", "Plato '" + nuevoPlato.getNombre().trim() + "' añadido a la carta.");

        return "redirect:/dueno/panel";
    }

    @PostMapping("/platos/{id}/anadir-existente")
    public String anadirPlatoExistente(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            // Poner plato retirado visible en carta
            Plato plato = platoService.anadirPlatoExistenteALaCarta(id);

            // Mensaje de éxito plato retirado añadido
            redirectAttributes.addFlashAttribute("mensaje", "Plato '" + plato.getNombre() + "' añadido de nuevo a la carta.");

        } catch (IllegalArgumentException | OperacionNoPermitidaException e) {

            // Manejo de errores
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

    // Carta ordenada por categoria (entrante, principal, postre y bebida) (platos en carta y etirados)
    private void cargarCarta(Model model){

        // Ordena los platos de la carta
        model.addAttribute("platos", platoService.listarCarta().stream()
        .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre)).toList());

        // Carga las categorías
        model.addAttribute("categorias", CategoriaPlato.values());

        // Carga los platos retirados de la carta ordenados
        model.addAttribute("platosRetirados", platoService.listarPlatosRetiradosDeLaCarta().stream()
        .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre)).toList());
    }
}