package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;

import es.upm.is2.sabordelhimalaya.dto.request.MenuDiarioDTO;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;

import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;

import es.upm.is2.sabordelhimalaya.repository.MenuDiarioOpcionRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuDiarioServiceImpl implements MenuDiarioService {

    // Precio fijo del menú diario
    private static final BigDecimal PRECIO_MENU = new BigDecimal("15.00");

    // Número de opciones por categoría
    private static final int NUM_ENTRANTES = 3;
    private static final int NUM_PRINCIPALES = 3;
    private static final int NUM_POSTRES = 2;

    private final MenuDiarioRepository menuDiarioRepository;
    private final MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    private final PlatoRepository platoRepository;

    @Override
    public Optional<MenuDiario> consultarMenuDeFecha(LocalDate fecha) {
        // Menú del día configurado para una fecha
        return menuDiarioRepository.findByFecha(fecha);
    }

    @Override
    public List<MenuDiarioOpcion> listarOpcionesDeMenu(Long idMenuDiario) {
        // Opciones de un menú diario
        return menuDiarioOpcionRepository.findByMenuDiarioIdMenuDiario(idMenuDiario);
    }

    @Override
    @Transactional
    public MenuDiario configurarMenu(MenuDiarioDTO dto) {

        // Validar que el DTO incluye una fecha
        if (dto == null || dto.getFecha() == null) {
            throw new IllegalArgumentException("Debes indicar la fecha del menú.");
        }

        LocalDate fecha = dto.getFecha();

        // No se puede configurar el menú de un día pasado
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede configurar el menú de un día que ya ha pasado.");
        }

        // Solo puede haber un menú configurado por día
        if (menuDiarioRepository.existsByFecha(fecha)) {
            throw new IllegalArgumentException("Ya hay un menú configurado para el día " + fecha + ".");
        }

        // Validar que el número y categoría de las opciones es correcto
        List<Plato> entrantes = obtenerPlatos(dto.getIdsEntrantes(), NUM_ENTRANTES, CategoriaPlato.ENTRANTE, "entrantes");
        List<Plato> principales = obtenerPlatos(dto.getIdsPrincipales(), NUM_PRINCIPALES, CategoriaPlato.PRINCIPAL, "platos principales");
        List<Plato> postres = obtenerPlatos(dto.getIdsPostres(), NUM_POSTRES, CategoriaPlato.POSTRE, "postres");

        // Crear menú del día y guardarlo
        MenuDiario menu = new MenuDiario();
        menu.setFecha(fecha);
        menu.setPrecio(PRECIO_MENU);
        MenuDiario menuGuardado = menuDiarioRepository.save(menu);

        // Guardar las opciones del menú
        List<MenuDiarioOpcion> opciones = new ArrayList<>();
        anadirOpcionesMenu(opciones, menuGuardado, entrantes, CategoriaMenu.ENTRANTE);
        anadirOpcionesMenu(opciones, menuGuardado, principales, CategoriaMenu.PRINCIPAL);
        anadirOpcionesMenu(opciones, menuGuardado, postres, CategoriaMenu.POSTRE);
        menuDiarioOpcionRepository.saveAll(opciones);

        return menuGuardado;
    }

    @Override
    public MenuDiario buscarPorId(Long id) {
        // TODO: return menuDiarioRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<MenuDiario> listarTodosMenuDiario() {
        // TODO: return menuDiarioRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }

    // ----------------------------------------------------------------
    // Métodos auxiliares
    // ----------------------------------------------------------------

    // Comprueba que hay exactamente 'cantidad' platos distintos, que existen y que son de la categoría indicada
    // Devuelve una lista de los platos del menú de una determinada categoría
    private List<Plato> obtenerPlatos(List<Long> ids, int cantidad, CategoriaPlato categoria, String nombreGrupo) {

        // Validar que llegan exactamente 'cantidad' Ids y ninguno es null
        if (ids == null || ids.size() != cantidad || ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Debes seleccionar " + cantidad + " " + nombreGrupo + ".");
        }

        // Validar que no se repite ningún plato entre las opciones
        if (new HashSet<>(ids).size() != cantidad) {
            throw new IllegalArgumentException("No puedes repetir el mismo plato en los " + nombreGrupo + ".");
        }

        // Cargar cada plato y comprobar que existe y que es de la categoría esperada
        List<Plato> platos = new ArrayList<>();

        for (Long id : ids) {

            Plato plato = platoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el plato con id " + id + "."));

            if (plato.getCategoria() != categoria) {
                throw new IllegalArgumentException("El plato '" + plato.getNombre() + "' no se puede usar en " + nombreGrupo + " porque es de la categoría " + plato.getCategoria() + ".");
            }

            platos.add(plato);
        }

        return platos;
    }

    // Crea las opciones de una categoría del menú del día y las añade a la lista opciones
    private void anadirOpcionesMenu(List<MenuDiarioOpcion> opciones, MenuDiario menu, List<Plato> platos, CategoriaMenu categoria) {

        for (Plato plato : platos) {

            MenuDiarioOpcion opcion = new MenuDiarioOpcion();

            opcion.setMenuDiario(menu);
            opcion.setPlato(plato);
            opcion.setCategoria(categoria);
            opciones.add(opcion);
        }
    }
}
