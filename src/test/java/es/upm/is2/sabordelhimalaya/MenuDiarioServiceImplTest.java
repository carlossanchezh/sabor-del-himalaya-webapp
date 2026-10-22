package es.upm.is2.sabordelhimalaya;

import es.upm.is2.sabordelhimalaya.dto.MenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioOpcionRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;
import es.upm.is2.sabordelhimalaya.service.MenuDiarioServiceImpl;

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

// Pruebas unitarias de US-13 (Chef configura el menú diario): MenuDiarioServiceImpl.configurarMenu()
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

    private LocalDate manana;

    @BeforeEach
    void setUp() {
        manana = LocalDate.now().plusDays(1);
    }

    // Platos de prueba: ids 1-3 entrantes, 4-6 principales, 7-8 postres
    private Plato plato(long id, CategoriaPlato categoria) {
        return new Plato(id, "Plato " + id, "Descripción " + id, new BigDecimal("10.00"), categoria, true, false);
    }

    private void prepararPlatosValidos() {
        for (long id = 1; id <= 3; id++) when(platoRepository.findById(id)).thenReturn(Optional.of(plato(id, CategoriaPlato.ENTRANTE)));
        for (long id = 4; id <= 6; id++) when(platoRepository.findById(id)).thenReturn(Optional.of(plato(id, CategoriaPlato.PRINCIPAL)));
        for (long id = 7; id <= 8; id++) when(platoRepository.findById(id)).thenReturn(Optional.of(plato(id, CategoriaPlato.POSTRE)));
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
        return dto(manana, Arrays.asList(1L, 2L, 3L), Arrays.asList(4L, 5L, 6L), Arrays.asList(7L, 8L));
    }

    @Test
    @DisplayName("CPU-13.1: Menú válido (3+3+2) se guarda con precio 15 € y 8 opciones")
    void configurarMenu_valido_guardaMenuYOpciones() {
        prepararPlatosValidos();
        when(menuDiarioRepository.save(any(MenuDiario.class))).thenAnswer(inv -> {
            MenuDiario m = inv.getArgument(0);
            m.setIdMenuDiario(1L);
            return m;
        });

        MenuDiario resultado = menuDiarioService.configurarMenu(dtoValido());

        assertEquals(manana, resultado.getFecha());
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
    @DisplayName("CPU-13.2: Rechaza el menú si no hay exactamente 3 entrantes")
    void configurarMenu_dosEntrantes_lanzaError() {
        MenuDiarioDTO dto = dto(manana, Arrays.asList(1L, 2L), Arrays.asList(4L, 5L, 6L), Arrays.asList(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("3 entrantes"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.3: Rechaza el menú si falta algún postre sin seleccionar")
    void configurarMenu_postreSinSeleccionar_lanzaError() {
        when(platoRepository.findById(anyLong())).thenAnswer(inv -> {
            long id = inv.getArgument(0);
            return Optional.of(plato(id, id <= 3 ? CategoriaPlato.ENTRANTE : CategoriaPlato.PRINCIPAL));
        });
        List<Long> postres = new ArrayList<>(Arrays.asList(7L, null)); // desplegable vacío
        MenuDiarioDTO dto = dto(manana, Arrays.asList(1L, 2L, 3L), Arrays.asList(4L, 5L, 6L), postres);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("2 postres"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.4: Rechaza el menú si se repite un plato en la misma categoría")
    void configurarMenu_platoRepetido_lanzaError() {
        MenuDiarioDTO dto = dto(manana, Arrays.asList(1L, 1L, 3L), Arrays.asList(4L, 5L, 6L), Arrays.asList(7L, 8L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dto));
        assertTrue(ex.getMessage().contains("repetir"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.5: Rechaza un postre seleccionado como entrante (categoría incorrecta)")
    void configurarMenu_categoriaIncorrecta_lanzaError() {
        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(7L)).thenReturn(Optional.of(plato(7L, CategoriaPlato.POSTRE)));
        MenuDiarioDTO dto = dto(manana, Arrays.asList(1L, 2L, 7L), Arrays.asList(4L, 5L, 6L), Arrays.asList(7L, 8L));

        assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dto));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.6: Rechaza configurar un día que ya tiene menú")
    void configurarMenu_fechaYaConfigurada_lanzaError() {
        when(menuDiarioRepository.existsByFecha(manana)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dtoValido()));
        assertTrue(ex.getMessage().contains("Ya hay un menú"));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.7: Rechaza configurar el menú de un día pasado")
    void configurarMenu_fechaPasada_lanzaError() {
        MenuDiarioDTO dto = dtoValido();
        dto.setFecha(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> menuDiarioService.configurarMenu(dto));
        verify(menuDiarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-13.8: Lanza RecursoNoEncontrado si un plato no existe")
    void configurarMenu_platoInexistente_lanzaRecursoNoEncontrado() {
        when(platoRepository.findById(1L)).thenReturn(Optional.of(plato(1L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(2L)).thenReturn(Optional.of(plato(2L, CategoriaPlato.ENTRANTE)));
        when(platoRepository.findById(99L)).thenReturn(Optional.empty());
        MenuDiarioDTO dto = dto(manana, Arrays.asList(1L, 2L, 99L), Arrays.asList(4L, 5L, 6L), Arrays.asList(7L, 8L));

        assertThrows(RecursoNoEncontradoException.class, () -> menuDiarioService.configurarMenu(dto));
        verify(menuDiarioRepository, never()).save(any());
    }
}
