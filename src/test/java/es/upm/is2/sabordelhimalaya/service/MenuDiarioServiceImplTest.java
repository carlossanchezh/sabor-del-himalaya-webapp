package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.dto.request.MenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioOpcionRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-05 (Consultar menú del día)
 - US-13 (Chef configura menú del día)
 */

@ExtendWith(MockitoExtension.class)
class MenuDiarioServiceImplTest {

    @Mock
    private MenuDiarioRepository menuDiarioRepository;
    @Mock
    private MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private MenuDiarioServiceImpl menuDiarioService;

    private static final List<Long> IDS_ENTRANTES   = List.of(1L, 2L, 3L);
    private static final List<Long> IDS_PRINCIPALES = List.of(4L, 5L, 6L);
    private static final List<Long> IDS_POSTRES     = List.of(7L, 8L);

    private LocalDate hoy;
    private MenuDiario menuDeHoy;

    @BeforeEach
    void setUp() {

        hoy = LocalDate.now();
        menuDeHoy = new MenuDiario(1L, hoy, new BigDecimal("15.00"));
    }

    private Plato plato(long id, CategoriaPlato categoria) {
        return new Plato(id, "Plato " + id, "Descripción " + id, new BigDecimal("10.00"), categoria, true, false);
    }

    private MenuDiarioDTO dto(LocalDate fecha, List<Long> entrantes, List<Long> principales, List<Long> postres) {

        MenuDiarioDTO dto = new MenuDiarioDTO();
        dto.setFecha(fecha);
        dto.setIdsEntrantes(entrantes);
        dto.setIdsPrincipales(principales);
        dto.setIdsPostres(postres);
        return dto;
    }

    private MenuDiarioDTO dtoValido() {

        return dto(hoy, IDS_ENTRANTES, IDS_PRINCIPALES, IDS_POSTRES);
    }

    private void prepararPlatosValidos() {

        IDS_ENTRANTES.forEach(id -> when(platoRepository.findById(id))
                .thenReturn(Optional.of(plato(id, CategoriaPlato.ENTRANTE))));
        IDS_PRINCIPALES.forEach(id -> when(platoRepository.findById(id))
                .thenReturn(Optional.of(plato(id, CategoriaPlato.PRINCIPAL))));
        IDS_POSTRES.forEach(id -> when(platoRepository.findById(id))
                .thenReturn(Optional.of(plato(id, CategoriaPlato.POSTRE))));
    }

    @Test
    @DisplayName("PU-05-01: consultarMenuDeFecha devuelve el menú si existe")
    void consultarMenuDeFecha_existe_devuelveMenu() {

        when(menuDiarioRepository.findByFecha(hoy)).thenReturn(Optional.of(menuDeHoy));

        Optional<MenuDiario> resultado = menuDiarioService.consultarMenuDeFecha(hoy);

        assertTrue(resultado.isPresent());
        assertSame(menuDeHoy, resultado.get());
        verify(menuDiarioRepository).findByFecha(hoy);
    }

    @Test
    @DisplayName("PU-05-02: consultarMenuDeFecha devuelve vacío si no hay menú")
    void consultarMenuDeFecha_noExiste_devuelveVacio() {

        when(menuDiarioRepository.findByFecha(hoy)).thenReturn(Optional.empty());

        assertTrue(menuDiarioService.consultarMenuDeFecha(hoy).isEmpty());
        verify(menuDiarioRepository).findByFecha(hoy);
    }

    @Test
    @DisplayName("PU-05-03: listarOpcionesDeMenu devuelve las opciones del menú")
    void listarOpcionesDeMenu_devuelveOpciones() {

        List<MenuDiarioOpcion> opciones = List.of(
                new MenuDiarioOpcion(1L, menuDeHoy, plato(1L, CategoriaPlato.ENTRANTE), CategoriaMenu.ENTRANTE),
                new MenuDiarioOpcion(2L, menuDeHoy, plato(2L, CategoriaPlato.ENTRANTE), CategoriaMenu.ENTRANTE));

        when(menuDiarioOpcionRepository.findByMenuDiarioIdMenuDiario(1L)).thenReturn(opciones);

        List<MenuDiarioOpcion> resultado = menuDiarioService.listarOpcionesDeMenu(1L);

        assertEquals(opciones, resultado);
        verify(menuDiarioOpcionRepository).findByMenuDiarioIdMenuDiario(1L);
    }

    @Test
    @DisplayName("PU-05-04: listarOpcionesDeMenu devuelve lista vacía si no hay opciones")
    void listarOpcionesDeMenu_sinOpciones_devuelveListaVacia() {

        when(menuDiarioOpcionRepository.findByMenuDiarioIdMenuDiario(99L)).thenReturn(List.of());

        assertTrue(menuDiarioService.listarOpcionesDeMenu(99L).isEmpty());
    }

    @Test
    @DisplayName("PU-13-01: Menú válido (3+3+2) se guarda con precio 15 € y 8 opciones")
    void configurarMenu_valido_guardaMenuYOpciones() {

        prepararPlatosValidos();
        when(menuDiarioRepository.save(any(MenuDiario.class))).thenAnswer(inv -> {
            MenuDiario m = inv.getArgument(0);
            m.setIdMenuDiario(1L);
            return m;
        });

        MenuDiario resultado = menuDiarioService.configurarMenu(dtoValido());

        assertEquals(hoy, resultado.getFecha());
        assertEquals(0, new BigDecimal("15.00").compareTo(resultado.getPrecio()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MenuDiarioOpcion>> captor = ArgumentCaptor.forClass(List.class);
        verify(menuDiarioOpcionRepository).saveAll(captor.capture());
        List<MenuDiarioOpcion> opciones = captor.getValue();

        assertEquals(8, opciones.size());
        assertEquals(3, opciones.stream().filter(o -> o.getCategoria() == CategoriaMenu.ENTRANTE).count());
        assertEquals(3, opciones.stream().filter(o -> o.getCategoria() == CategoriaMenu.PRINCIPAL).count());
        assertEquals(2, opciones.stream().filter(o -> o.getCategoria() == CategoriaMenu.POSTRE).count());
        assertTrue(opciones.stream().allMatch(o -> o.getMenuDiario() == resultado));
    }

    @Test
    @DisplayName("PU-13-02: Rechaza un menú con fecha pero sin opciones seleccionadas")
    void configurarMenu_sinOpciones_lanzaError() {

        MenuDiarioDTO dto = new MenuDiarioDTO();
        dto.setFecha(hoy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("seleccionar"));
        assertTrue(ex.getMessage().contains("entrantes"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-03: Rechaza un menú sin fecha")
    void configurarMenu_fechaNula_lanzaError() {

        MenuDiarioDTO dto = new MenuDiarioDTO();
        dto.setIdsEntrantes(IDS_ENTRANTES);
        dto.setIdsPrincipales(IDS_PRINCIPALES);
        dto.setIdsPostres(IDS_POSTRES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("fecha"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-04: Rechaza el menú si no hay exactamente 3 entrantes")
    void configurarMenu_dosEntrantes_lanzaError() {

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L), IDS_PRINCIPALES, IDS_POSTRES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("3 entrantes"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-05: Rechaza el menú si no hay exactamente 3 principales")
    void configurarMenu_dosPrincipales_lanzaError() {

        when(platoRepository.findById(anyLong())).thenAnswer(inv ->
                Optional.of(plato(inv.getArgument(0), CategoriaPlato.ENTRANTE)));

        MenuDiarioDTO dto = dto(hoy, IDS_ENTRANTES, List.of(4L, 5L), IDS_POSTRES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("3 platos principales"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-06: Rechaza el menú si no hay exactamente 2 postres")
    void configurarMenu_unPostre_lanzaError() {

        when(platoRepository.findById(anyLong())).thenAnswer(inv -> {
            long id = inv.getArgument(0);
            CategoriaPlato categoria = (id <= 3) ? CategoriaPlato.ENTRANTE : CategoriaPlato.PRINCIPAL;
            return Optional.of(plato(id, categoria));
        });

        MenuDiarioDTO dto = dto(hoy, IDS_ENTRANTES, IDS_PRINCIPALES, List.of(7L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("2 postres"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-07: Rechaza el menú si falta algún entrante sin seleccionar")
    void configurarMenu_entranteSinSeleccionar_lanzaError() {

        List<Long> entrantes = new ArrayList<>(Arrays.asList(1L, 2L, null));
        MenuDiarioDTO dto = dto(hoy, entrantes, IDS_PRINCIPALES, IDS_POSTRES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("3 entrantes"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-08: Rechaza el menú si falta algún principal sin seleccionar")
    void configurarMenu_principalSinSeleccionar_lanzaError() {

        when(platoRepository.findById(anyLong())).thenAnswer(inv ->
                Optional.of(plato(inv.getArgument(0), CategoriaPlato.ENTRANTE)));

        List<Long> principales = new ArrayList<>(Arrays.asList(4L, 5L, null));
        MenuDiarioDTO dto = dto(hoy, IDS_ENTRANTES, principales, IDS_POSTRES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("platos principales"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-09: Rechaza el menú si falta algún postre sin seleccionar")
    void configurarMenu_postreSinSeleccionar_lanzaError() {

        when(platoRepository.findById(anyLong())).thenAnswer(inv -> {
            long id = inv.getArgument(0);
            CategoriaPlato categoria = (id <= 3) ? CategoriaPlato.ENTRANTE : CategoriaPlato.PRINCIPAL;
            return Optional.of(plato(id, categoria));
        });

        List<Long> postres = new ArrayList<>(Arrays.asList(7L, null));
        MenuDiarioDTO dto = dto(hoy, IDS_ENTRANTES, IDS_PRINCIPALES, postres);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("2 postres"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-10: Rechaza el menú si se repite un plato en la misma categoría")
    void configurarMenu_platoRepetido_lanzaError() {

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 1L, 3L), List.of(4L, 5L, 6L), List.of(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("repetir"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-01: Rechaza un entrante seleccionado como principal")
    void configurarMenu_entranteComoPrincipal_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(3L)).thenReturn(Optional.of(plato(3L, CategoriaPlato.ENTRANTE)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 3L), List.of(1L, 5L, 6L), List.of(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en platos principales"));
        assertTrue(ex.getMessage().contains("ENTRANTE"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-02: Rechaza un entrante seleccionado como postre")
    void configurarMenu_entranteComoPostre_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(3L)).thenReturn(Optional.of(plato(3L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(4L)).thenReturn(Optional.of(plato(4L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(5L)).thenReturn(Optional.of(plato(5L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(6L)).thenReturn(Optional.of(plato(6L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(7L)).thenReturn(Optional.of(plato(7L, CategoriaPlato.POSTRE)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 3L), List.of(4L, 5L, 6L), List.of(7L, 1L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en postres"));
        assertTrue(ex.getMessage().contains("ENTRANTE"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-03: Rechaza un principal seleccionado como entrante")
    void configurarMenu_principalComoEntrante_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(4L)).thenReturn(Optional.of(plato(4L, CategoriaPlato.PRINCIPAL)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 4L), List.of(4L, 5L, 6L), List.of(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en entrantes"));
        assertTrue(ex.getMessage().contains("PRINCIPAL"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-04: Rechaza un principal seleccionado como postre")
    void configurarMenu_principalComoPostre_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(3L)).thenReturn(Optional.of(plato(3L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(4L)).thenReturn(Optional.of(plato(4L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(5L)).thenReturn(Optional.of(plato(5L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(6L)).thenReturn(Optional.of(plato(6L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(7L)).thenReturn(Optional.of(plato(7L, CategoriaPlato.POSTRE)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 3L), List.of(4L, 5L, 6L), List.of(7L, 4L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en postres"));
        assertTrue(ex.getMessage().contains("PRINCIPAL"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-05: Rechaza un postre seleccionado como entrante")
    void configurarMenu_postreComoEntrante_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(7L)).thenReturn(Optional.of(plato(7L, CategoriaPlato.POSTRE)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 7L), List.of(4L, 5L, 6L), List.of(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en entrantes"));
        assertTrue(ex.getMessage().contains("POSTRE"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-11-06: Rechaza un postre seleccionado como principal")
    void configurarMenu_postreComoPrincipal_lanzaError() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(3L)).thenReturn(Optional.of(plato(3L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(4L)).thenReturn(Optional.of(plato(4L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(5L)).thenReturn(Optional.of(plato(5L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(7L)).thenReturn(Optional.of(plato(7L, CategoriaPlato.POSTRE)));

        MenuDiarioDTO dto = dto(hoy, List.of(1L, 2L, 3L), List.of(4L, 5L, 7L), List.of(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("no se puede usar en platos principales"));
        assertTrue(ex.getMessage().contains("POSTRE"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-12: Rechaza configurar un día que ya tiene menú")
    void configurarMenu_fechaYaConfigurada_lanzaError() {

        when(menuDiarioRepository.existsByFecha(hoy)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dtoValido()));
        assertTrue(ex.getMessage().contains("Ya hay un menú"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-13: Rechaza configurar el menú de un día pasado")
    void configurarMenu_fechaPasada_lanzaError() {

        MenuDiarioDTO dto = dtoValido();
        dto.setFecha(LocalDate.now().minusDays(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("pasado"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-14: Rechaza dto nulo")
    void configurarMenu_dtoNulo_lanzaError() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuDiarioService.configurarMenu(null));
        assertTrue(ex.getMessage().toLowerCase().contains("fecha"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-13-15: Acepta platos fuera de carta del chef y platos retirados de la carta (CA13.4, BUG-05)")
    void configurarMenu_conPlatosRetiradosYDelChef_seGuarda() {

        // Entrantes: 1 de la carta, 1 retirado de la carta, 1 creado por el chef
        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(
                new Plato(2L, "Retirado 2", "Descripción 2", new BigDecimal("10.00"), CategoriaPlato.ENTRANTE, false, false)));
        when(platoRepository.findById(3L)).thenReturn(Optional.of(
                new Plato(3L, "Del chef 3", "Descripción 3", new BigDecimal("0.00"), CategoriaPlato.ENTRANTE, false, true)));
        // Principales: 1 retirado de la carta y 2 de la carta
        when(platoRepository.findById(4L)).thenReturn(Optional.of(
                new Plato(4L, "Retirado 4", "Descripción 4", new BigDecimal("12.00"), CategoriaPlato.PRINCIPAL, false, false)));
        when(platoRepository.findById(5L)).thenReturn(Optional.of(plato(5L, CategoriaPlato.PRINCIPAL)));
        when(platoRepository.findById(6L)).thenReturn(Optional.of(plato(6L, CategoriaPlato.PRINCIPAL)));
        // Postres: 1 retirado de la carta y 1 de la carta
        when(platoRepository.findById(7L)).thenReturn(Optional.of(
                new Plato(7L, "Retirado 7", "Descripción 7", new BigDecimal("4.00"), CategoriaPlato.POSTRE, false, false)));
        when(platoRepository.findById(8L)).thenReturn(Optional.of(plato(8L, CategoriaPlato.POSTRE)));
        when(menuDiarioRepository.save(any(MenuDiario.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDiario resultado = menuDiarioService.configurarMenu(dtoValido());

        assertEquals(hoy, resultado.getFecha());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MenuDiarioOpcion>> captor = ArgumentCaptor.forClass(List.class);
        verify(menuDiarioOpcionRepository).saveAll(captor.capture());
        assertEquals(8, captor.getValue().size());
    }
}
