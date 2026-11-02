package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.MenuDiarioService;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/carta")
@RequiredArgsConstructor
public class CartaController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    private final PlatoService platoService;
    private final MenuDiarioService menuDiarioService;

    @GetMapping
    public String mostrarCarta(Model model) {

        // Carta agrupada por categorías
        Map<String, List<Plato>> carta = new LinkedHashMap<>();
        carta.put("Entrantes", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.ENTRANTE)));
        carta.put("Principales", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL)));
        carta.put("Postres", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.POSTRE)));
        carta.put("Bebidas", ordenarPlatos(platoService.listarCartaPorCategoria(CategoriaPlato.BEBIDA)));
        model.addAttribute("carta", carta);

        // Fecha del día de hoy
        LocalDate hoy = LocalDate.now();
        model.addAttribute("fechaTexto", formatearFecha(hoy));

        // Menú diario del día de hoy
        Optional<MenuDiario> menu = menuDiarioService.consultarMenuDeFecha(hoy);

        // Si no hay menú configurado, la vista muestra el aviso de no disponible
        model.addAttribute("menuDisponible", menu.isPresent());

        if (menu.isPresent()) {
            MenuDiario menuDiario = menu.get();
            model.addAttribute("menu", menuDiario);

            // Opciones del menú del día para elegir
            List<MenuDiarioOpcion> opciones = menuDiarioService.listarOpcionesDeMenu(menuDiario.getIdMenuDiario());
            model.addAttribute("entrantesMenu", opcionesDeCategoria(opciones, CategoriaMenu.ENTRANTE));
            model.addAttribute("principalesMenu", opcionesDeCategoria(opciones, CategoriaMenu.PRINCIPAL));
            model.addAttribute("postresMenu", opcionesDeCategoria(opciones, CategoriaMenu.POSTRE));
        }

        return "carta/index";
    }

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    // Filtra las opciones del menú por categoría
    private List<Plato> opcionesDeCategoria(List<MenuDiarioOpcion> opciones, CategoriaMenu categoria) {

        return opciones.stream()
                .filter(opcion -> opcion.getCategoria() == categoria)
                .map(MenuDiarioOpcion::getPlato)
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
