package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.dto.request.MenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.MenuDiarioService;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    private final PlatoService platoService;
    private final MenuDiarioService menuDiarioService;

    @GetMapping("/carta")
    public String verCarta(Model model) {

        // Carta agrupada por categorías, incluidas las bebidas
        Map<String, List<Plato>> carta = new LinkedHashMap<>();
        carta.put("Entrantes", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.ENTRANTE)));
        carta.put("Principales", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL)));
        carta.put("Postres", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.POSTRE)));
        carta.put("Bebidas", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.BEBIDA)));
        model.addAttribute("carta", carta);

        // Sección de Menú diario (el botón de configurar solo aparece si hoy no hay menú)
        LocalDate hoy = LocalDate.now();
        model.addAttribute("fechaHoy", formatearFecha(hoy));
        model.addAttribute("menuConfigurado", menuDiarioService.consultarMenuDeFecha(hoy).isPresent());
        return "chef/carta";
    }

    @GetMapping("/menu")
    public String configurarMenu(Model model, RedirectAttributes redirectAttributes) {

        // Fecha del día de hoy
        LocalDate hoy = LocalDate.now();

        // Si ya existe un menú para hoy, no se puede volver a configurar
        if (menuDiarioService.consultarMenuDeFecha(hoy).isPresent()) {

            redirectAttributes.addFlashAttribute("mensaje", "El menú del " + formatearFecha(hoy) + " ya está configurado.");
            return "redirect:/chef/carta";
        }

        // Creación del menú vacío para rellenarlo desde la interfáz
        MenuDiarioDTO menu = new MenuDiarioDTO();
        menu.setFecha(hoy);
        model.addAttribute("menu", menu);

        // Carga la lista de platos seleccionables para configurar el menú
        cargarDatosFormulario(model, hoy);

        return "chef/menu";
    }

    @PostMapping("/menu")
    public String guardarMenu(@Valid @ModelAttribute("menu") MenuDiarioDTO menu, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        // Fecha del día de hoy
        LocalDate hoy = LocalDate.now();

        // Se reactualiza la fecha del menú
        menu.setFecha(hoy);

        // Errores de binding
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Los datos del formulario no son válidos. Revisa la selección.");
            cargarDatosFormulario(model, hoy);
            return "chef/menu";
        }

        try {

            // Configuración del menú del día
            menuDiarioService.configurarMenu(menu);

        } catch (IllegalArgumentException e) {

            // Errores de validación (faltan platos, repetidos, categoría incorrecta, ya configurado...)
            model.addAttribute("error", e.getMessage());
            cargarDatosFormulario(model, hoy);
            return "chef/menu";
        }

        //Mensaje de éxito al configurar el menú
        redirectAttributes.addFlashAttribute("mensaje", "Menú del " + formatearFecha(hoy) + " guardado y publicado.");

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

    // Listas de los desplegables de cada categoría:
    //  - platos de la carta
    //  - platos fuera de carta creados por el chef
    //  - platos del restaurante retirados de la carta
    private void cargarDatosFormulario(Model model, LocalDate hoy) {

        // Se cargan todos los platos del sistema
        List<Plato> todos = platoService.listarTodosPlatos();

        // Entrantes: carta + fuera de carta del chef + retirados
        model.addAttribute("entrantesCarta", filtrarCarta(todos, CategoriaPlato.ENTRANTE));
        model.addAttribute("entrantesChef", filtrarChef(todos, CategoriaPlato.ENTRANTE));
        model.addAttribute("entrantesRetirados", filtrarRetirados(todos, CategoriaPlato.ENTRANTE));

        // Principales: carta + fuera de carta del chef + retirados
        model.addAttribute("principalesCarta", filtrarCarta(todos, CategoriaPlato.PRINCIPAL));
        model.addAttribute("principalesChef", filtrarChef(todos, CategoriaPlato.PRINCIPAL));
        model.addAttribute("principalesRetirados", filtrarRetirados(todos, CategoriaPlato.PRINCIPAL));

        // Postres: carta + fuera de carta del chef + retirados
        model.addAttribute("postresCarta", filtrarCarta(todos, CategoriaPlato.POSTRE));
        model.addAttribute("postresChef", filtrarChef(todos, CategoriaPlato.POSTRE));
        model.addAttribute("postresRetirados", filtrarRetirados(todos, CategoriaPlato.POSTRE));

        // Fecha formateada
        model.addAttribute("fechaTexto", formatearFecha(hoy));
    }

    // Platos visibles en la carta (esta_en_carta=true) de una categoría
    private List<Plato> filtrarCarta(List<Plato> platos, CategoriaPlato categoria) {

        return platos.stream()
                .filter(p -> p.getCategoria() == categoria)
                .filter(p -> Boolean.TRUE.equals(p.getEstaEnCarta()))
                .sorted(Comparator.comparing(Plato::getNombre))
                .toList();
    }

    // Platos fuera de carta creados por el chef (esta_en_carta=false Y es_del_chef=true) de una categoría
    private List<Plato> filtrarChef(List<Plato> platos, CategoriaPlato categoria) {

        return platos.stream()
                .filter(p -> p.getCategoria() == categoria)
                .filter(p -> !Boolean.TRUE.equals(p.getEstaEnCarta()))
                .filter(p -> Boolean.TRUE.equals(p.getEsDelChef()))
                .sorted(Comparator.comparing(Plato::getNombre))
                .toList();
    }

    // Platos del restaurante retirados de la carta (esta_en_carta=false Y es_del_chef=false) de una categoría (BUG-05, CA13.4)
    private List<Plato> filtrarRetirados(List<Plato> platos, CategoriaPlato categoria) {

        return platos.stream()
                .filter(p -> p.getCategoria() == categoria)
                .filter(p -> !Boolean.TRUE.equals(p.getEstaEnCarta()))
                .filter(p -> !Boolean.TRUE.equals(p.getEsDelChef()))
                .sorted(Comparator.comparing(Plato::getNombre))
                .toList();
    }

    // Ordenar una lista de platos alfabéticamente por nombre
    private List<Plato> ordenarPlatos(List<Plato> platos) {

        return platos.stream()
                .sorted(Comparator.comparing(Plato::getNombre))
                .toList();
    }

    // Formatea una fecha como "EEEE, d 'de' MMMM 'de' yyyy" (primera letra mayúscula)
    private String formatearFecha(LocalDate fecha) {

        String texto = fecha.format(FORMATO_FECHA);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
