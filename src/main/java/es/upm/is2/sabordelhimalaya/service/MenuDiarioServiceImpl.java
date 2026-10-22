package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;

import es.upm.is2.sabordelhimalaya.dto.MenuDiarioDTO;

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

    // Precio fijo del menú diario (enunciado: 15 €)
    private static final BigDecimal PRECIO_MENU = new BigDecimal("15.00");

    // Número de opciones por categoría (enunciado: 3 entrantes, 3 principales y 2 postres)
    private static final int NUM_ENTRANTES = 3;
    private static final int NUM_PRINCIPALES = 3;
    private static final int NUM_POSTRES = 2;

    private final MenuDiarioRepository menuDiarioRepository;
    private final MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    private final PlatoRepository platoRepository;

    @Override
    public Optional<MenuDiario> consultarMenuDeFecha(LocalDate fecha) {
        // TODO (US-05): return menuDiarioRepository.findByFecha(fecha);
        throw new UnsupportedOperationException("TODO US-05");
    }

    // US-13: Chef configurar los platos del menú diario
    @Override
    @Transactional
    public MenuDiario configurarMenu(MenuDiarioDTO dto) {
        if (dto == null || dto.getFecha() == null) {
            throw new IllegalArgumentException("Debes indicar la fecha del menú.");
        }
        LocalDate fecha = dto.getFecha();
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede configurar el menú de un día que ya ha pasado.");
        }
        // CA13.5: solo puede haber un menú por día (la columna fecha es UNIQUE en la BD)
        if (menuDiarioRepository.existsByFecha(fecha)) {
            throw new IllegalArgumentException("Ya hay un menú configurado para el día " + fecha + ".");
        }

        // CA13.1 - CA13.4: validar número de opciones y categoría de cada plato
        List<Plato> entrantes = obtenerPlatos(dto.getIdsEntrantes(), NUM_ENTRANTES, CategoriaPlato.ENTRANTE, "entrantes");
        List<Plato> principales = obtenerPlatos(dto.getIdsPrincipales(), NUM_PRINCIPALES, CategoriaPlato.PRINCIPAL, "platos principales");
        List<Plato> postres = obtenerPlatos(dto.getIdsPostres(), NUM_POSTRES, CategoriaPlato.POSTRE, "postres");

        // Guardar el menú del día
        MenuDiario menu = new MenuDiario();
        menu.setFecha(fecha);
        menu.setPrecio(PRECIO_MENU);
        MenuDiario menuGuardado = menuDiarioRepository.save(menu);

        // Guardar las opciones del menú (una fila por plato elegido)
        List<MenuDiarioOpcion> opciones = new ArrayList<>();
        anadirOpciones(opciones, menuGuardado, entrantes, CategoriaMenu.ENTRANTE);
        anadirOpciones(opciones, menuGuardado, principales, CategoriaMenu.PRINCIPAL);
        anadirOpciones(opciones, menuGuardado, postres, CategoriaMenu.POSTRE);
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
    // Métodos auxiliares de configurarMenu (US-13)
    // ----------------------------------------------------------------

    // Comprueba que hay exactamente 'cantidad' platos distintos, que existen y que son de la categoría indicada
    private List<Plato> obtenerPlatos(List<Long> ids, int cantidad, CategoriaPlato categoria, String nombreGrupo) {
        if (ids == null || ids.size() != cantidad || ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Debes seleccionar " + cantidad + " " + nombreGrupo + ".");
        }
        if (new HashSet<>(ids).size() != cantidad) {
            throw new IllegalArgumentException("No puedes repetir el mismo plato en los " + nombreGrupo + ".");
        }

        List<Plato> platos = new ArrayList<>();
        for (Long id : ids) {
            Plato plato = platoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el plato con id " + id + "."));
            if (plato.getCategoria() != categoria) {
                throw new IllegalArgumentException("El plato '" + plato.getNombre() + "' no se puede usar en "
                        + nombreGrupo + " porque es de la categoría " + plato.getCategoria() + ".");
            }
            platos.add(plato);
        }
        return platos;
    }

    private void anadirOpciones(List<MenuDiarioOpcion> opciones, MenuDiario menu,
                                List<Plato> platos, CategoriaMenu categoria) {
        for (Plato plato : platos) {
            MenuDiarioOpcion opcion = new MenuDiarioOpcion();
            opcion.setMenuDiario(menu);
            opcion.setPlato(plato);
            opcion.setCategoria(categoria);
            opciones.add(opcion);
        }
    }
}
