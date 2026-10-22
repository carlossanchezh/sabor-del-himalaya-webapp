package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.MenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.MenuDiarioService;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/chef")
@RequiredArgsConstructor
public class ChefController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    private final PlatoService platoService;
    private final MenuDiarioService menuDiarioService;

    // US-12 / P14: Panel del chef (carta de solo lectura + estado del menú de hoy)
    @GetMapping("/carta")
    public String verCarta(Model model) {
        // CA12.1 y CA12.3: carta agrupada por categorías, incluidas las bebidas
        Map<String, List<Plato>> carta = new LinkedHashMap<>();
        carta.put("Entrantes", platoService.listarCartaPorCategoria(CategoriaPlato.ENTRANTE));
        carta.put("Principales", platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL));
        carta.put("Postres", platoService.listarCartaPorCategoria(CategoriaPlato.POSTRE));
        carta.put("Bebidas", platoService.listarCartaPorCategoria(CategoriaPlato.BEBIDA));
        model.addAttribute("carta", carta);

        // Recuadro "Menú diario": el botón de configurar solo aparece si hoy no hay menú
        LocalDate hoy = LocalDate.now();
        model.addAttribute("fechaHoy", formatearFecha(hoy));
        model.addAttribute("menuConfigurado", menuDiarioService.consultarMenuDeFecha(hoy).isPresent());
        return "chef/carta";
    }

    // US-13 / P15-P16: Formulario de configuración del menú diario
    @GetMapping("/menu")
    public String configurarMenu(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                 Model model, RedirectAttributes redirectAttributes) {
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();

        if (menuDiarioService.consultarMenuDeFecha(dia).isPresent()) {
            redirectAttributes.addFlashAttribute("mensaje", "El menú del " + formatearFecha(dia) + " ya está configurado.");
            return "redirect:/chef/carta";
        }

        MenuDiarioDTO menu = new MenuDiarioDTO();
        menu.setFecha(dia);
        model.addAttribute("menu", menu);
        cargarDatosFormulario(model, dia);
        return "chef/menu";
    }

    // US-13: Guardar el menú configurado y publicarlo
    @PostMapping("/menu")
    public String guardarMenu(@ModelAttribute("menu") MenuDiarioDTO menu, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        LocalDate dia = (menu.getFecha() != null) ? menu.getFecha() : LocalDate.now();

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Los datos del formulario no son válidos. Revisa la selección.");
            cargarDatosFormulario(model, dia);
            return "chef/menu";
        }

        try {
            menuDiarioService.configurarMenu(menu);
        } catch (IllegalArgumentException e) {
            // Errores de validación (faltan platos, repetidos, categoría incorrecta, ya configurado...)
            model.addAttribute("error", e.getMessage());
            cargarDatosFormulario(model, dia);
            return "chef/menu";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Menú del " + formatearFecha(dia) + " guardado y publicado.");
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

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    // Listas de los desplegables: platos de la carta y platos fuera de carta (CA13.4) de cada categoría
    private void cargarDatosFormulario(Model model, LocalDate dia) {
        List<Plato> todos = platoService.listarTodosPlatos();

        model.addAttribute("entrantesCarta", filtrar(todos, CategoriaPlato.ENTRANTE, true));
        model.addAttribute("entrantesFueraCarta", filtrar(todos, CategoriaPlato.ENTRANTE, false));
        model.addAttribute("principalesCarta", filtrar(todos, CategoriaPlato.PRINCIPAL, true));
        model.addAttribute("principalesFueraCarta", filtrar(todos, CategoriaPlato.PRINCIPAL, false));
        model.addAttribute("postresCarta", filtrar(todos, CategoriaPlato.POSTRE, true));
        model.addAttribute("postresFueraCarta", filtrar(todos, CategoriaPlato.POSTRE, false));

        model.addAttribute("fechaTexto", formatearFecha(dia));
    }

    private List<Plato> filtrar(List<Plato> platos, CategoriaPlato categoria, boolean enCarta) {
        return platos.stream()
                .filter(p -> p.getCategoria() == categoria)
                .filter(p -> Boolean.valueOf(enCarta).equals(p.getEstaEnCarta()))
                .sorted(Comparator.comparing(Plato::getNombre))
                .toList();
    }

    private String formatearFecha(LocalDate fecha) {
        String texto = fecha.format(FORMATO_FECHA);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1); // "Jueves, 22 de octubre de 2026"
    }
}
