package es.upm.is2.sabordelhimalaya.controller;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.service.MenuDiarioService;
import es.upm.is2.sabordelhimalaya.service.PlatoService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/carta")
@RequiredArgsConstructor
public class CartaController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    private final PlatoService platoService;
    private final MenuDiarioService menuDiarioService;

    // P5 + P6: una única pantalla con pestañas (carta por categorías y menú del día)
    @GetMapping
    public String mostrarCarta(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {

        // US-04 (CA4.1, CA4.2, CA4.3): carta agrupada por categorías, incluidas las bebidas
        Map<String, List<Plato>> carta = new LinkedHashMap<>();
        carta.put("Entrantes", platoService.listarCartaPorCategoria(CategoriaPlato.ENTRANTE));
        carta.put("Principales", platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL));
        carta.put("Postres", platoService.listarCartaPorCategoria(CategoriaPlato.POSTRE));
        carta.put("Bebidas", platoService.listarCartaPorCategoria(CategoriaPlato.BEBIDA));
        model.addAttribute("carta", carta);

        // US-05 (CA5.1): menú diario del día consultado (por defecto, hoy)
        LocalDate dia = (fecha != null) ? fecha : LocalDate.now();
        model.addAttribute("fechaMenu", dia);
        model.addAttribute("fechaTexto", formatearFecha(dia));

        Optional<MenuDiario> menu = menuDiarioService.consultarMenuDeFecha(dia);
        // CA5.5: si no hay menú configurado, la vista muestra el aviso de no disponible
        model.addAttribute("menuDisponible", menu.isPresent());
        if (menu.isPresent()) {
            MenuDiario menuDiario = menu.get();
            model.addAttribute("menu", menuDiario);

            // CA5.2 y CA5.4: entrantes, principales y postres entre los que elegir
            List<MenuDiarioOpcion> opciones =
                    menuDiarioService.listarOpcionesDeMenu(menuDiario.getIdMenuDiario());
            model.addAttribute("entrantesMenu", platosDe(opciones, CategoriaMenu.ENTRANTE));
            model.addAttribute("principalesMenu", platosDe(opciones, CategoriaMenu.PRINCIPAL));
            model.addAttribute("postresMenu", platosDe(opciones, CategoriaMenu.POSTRE));
        }

        return "carta/index";
    }

    private List<Plato> platosDe(List<MenuDiarioOpcion> opciones, CategoriaMenu categoria) {
        return opciones.stream()
                .filter(opcion -> opcion.getCategoria() == categoria)
                .map(MenuDiarioOpcion::getPlato)
                .toList();
    }

    private String formatearFecha(LocalDate fecha) {
        String texto = fecha.format(FORMATO_FECHA);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
