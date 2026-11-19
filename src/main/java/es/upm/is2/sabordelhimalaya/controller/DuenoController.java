package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.request.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.dto.request.PlatoModificacionDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.PedidoService;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/dueno")
@RequiredArgsConstructor 
public class DuenoController {

    private final PlatoService platoService;
    private final PedidoService pedidoService;

    @GetMapping("/panel")
    public String panel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, Model model) {

        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();

        model.addAttribute("fechaConsultada", dia);
        model.addAttribute("esHoy", dia.equals(LocalDate.now()));
        model.addAttribute("facturacionDia", pedidoService.calcularFacturacion(dia));

        // Facturación de los últimos 7 días
        cargarFacturacionSemanal(model);

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

            // Carga la facturación del día
            cargarFacturacion(model);

            // Facturación de los últimos 7 días
            cargarFacturacionSemanal(model);

            // Carga los platos del sistema
            cargarCarta(model);

            return "dueno/panel";
        }

        try {

            // Añadir plato nuevo a la carta
            platoService.anadirPlatoALaCarta(nuevoPlato);

        } catch (IllegalArgumentException e) {

            // Manejo de excepciones
            redirectAttributes.addFlashAttribute("errorCarta", e.getMessage());

            return "redirect:/dueno/panel";
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
    public String guardarEdicionPlato(@PathVariable Long id, @Valid @ModelAttribute("platoEditado") PlatoModificacionDTO platoEditado, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // El plato que se modifica es el de la URL
        platoEditado.setIdPlato(id);

        // Validacion de formulario
        if(bindingResult.hasErrors()){

            // Informa de los errores y abre modal de edicion
            model.addAttribute("mostrarModalEditar", true);
            cargarFacturacion(model);
            cargarFacturacionSemanal(model);
            cargarCarta(model);
            return "dueno/panel";
        }

        try {
            // Guardar cambios del plato modificado
            Plato plato = platoService.modificarPlato(platoEditado);
            redirectAttributes.addFlashAttribute("mensaje", "Producto '" + plato.getNombre() + "' modificado.");            
        } catch (IllegalArgumentException | OperacionNoPermitidaException e) {
            redirectAttributes.addFlashAttribute("errorCarta", e.getMessage());
        }
        return "redirect:/dueno/panel";
    }


    @PostMapping("/platos/{id}/eliminar")
    public String eliminarPlato(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Nombre para el mensaje
            String nombre = platoService.buscarPorId(id).getNombre();
            // Retirar plato de la carta
            platoService.retirarPlatoDeLaCarta(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto '" + nombre + "' retirado de la carta. Puedes volver a añadirlo desde 'Añadir producto'.");
        } catch (IllegalArgumentException | OperacionNoPermitidaException e) {
            redirectAttributes.addFlashAttribute("errorCarta", e.getMessage());
        }
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

        // Formularios vacios de los pop-ups si no vienen con los datos y errores de un envio
        if(!model.containsAttribute("nuevoPlato")){
            model.addAttribute("nuevoPlato", new PlatoCreacionDTO());
        }
        if(!model.containsAttribute("platoEditado")){
            model.addAttribute("platoEditado", new PlatoModificacionDTO());
        }
        
        // Carga los platos retirados de la carta ordenados
        model.addAttribute("platosRetirados", platoService.listarPlatosRetiradosDeLaCarta().stream()
        .sorted(Comparator.comparing(Plato::getCategoria).thenComparing(Plato::getNombre)).toList());
    }

    // Facturación del día
    private void cargarFacturacion(Model model) {

        LocalDate dia = LocalDate.now();

        model.addAttribute("fechaConsultada", dia);
        model.addAttribute("esHoy", true);
        model.addAttribute("facturacionDia", pedidoService.calcularFacturacion(dia));
    }

    // Facturación de los últimos 7 días
    private void cargarFacturacionSemanal(Model model) {

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");

        List<String> etiquetas = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        // Del día más antiguo (hace 6 días) al más reciente (hoy)
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            etiquetas.add(dia.format(formato));
            valores.add(pedidoService.calcularFacturacion(dia).doubleValue());
        }

        model.addAttribute("graficaEtiquetas", etiquetas);
        model.addAttribute("graficaValores", valores);
    }
}