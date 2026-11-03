package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.dto.request.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.Plato;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-04 (Cliente ver carta)
 - US-12 (Chef ver carta)
 - US-15 (Dueño ver carta)
 - US-17 (Dueño añadir plato)
 */

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private PlatoServiceImpl platoService;

    // Fixtures comunes reutilizados por varios tests
    private Plato platoRetirado;   // id=1, estaEnCarta=false, esDelChef=false
    private Plato platoDelChef;    // id=2, estaEnCarta=false, esDelChef=true
    private Plato platoEnCarta;    // id=3, estaEnCarta=true,  esDelChef=false
    private Plato platoEnCarta2;   // id=4, estaEnCarta=true,  esDelChef=false

    private PlatoCreacionDTO dtoValido;

    @BeforeEach
    void setUp() {

        platoRetirado = new Plato(1L, "Plato 1", "Descripción 1",
                new BigDecimal("10.00"), CategoriaPlato.PRINCIPAL, false, false);
        platoDelChef  = new Plato(2L, "Plato 2", "Descripción 2",
                new BigDecimal("10.00"), CategoriaPlato.PRINCIPAL, false, true);
        platoEnCarta  = new Plato(3L, "Plato 3", "Descripción 3",
                new BigDecimal("10.00"), CategoriaPlato.PRINCIPAL, true, false);
        platoEnCarta2 = new Plato(4L, "Plato 4", "Descripción 4",
                new BigDecimal("10.00"), CategoriaPlato.PRINCIPAL, true, false);

        dtoValido = new PlatoCreacionDTO();
        dtoValido.setNombre("Plato 5");
        dtoValido.setDescripcion("Descripción 5");
        dtoValido.setPrecio(new BigDecimal("10.00"));
        dtoValido.setCategoria(CategoriaPlato.ENTRANTE);

    }

    private PlatoCreacionDTO dto(String nombre, String descripcion, String precio, CategoriaPlato categoria) {

        PlatoCreacionDTO dto = new PlatoCreacionDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecio(precio != null ? new BigDecimal(precio) : null);
        dto.setCategoria(categoria);
        return dto;
    }

    @Test
    @DisplayName("CPU-04.1 / CPU-12.1 / CPU-15.1: listarCarta devuelve solo los platos visibles en carta")
    void listarCarta_devuelveLosPlatosDeLaCarta() {

        List<Plato> enCarta = List.of(platoEnCarta, platoEnCarta2);
        when(platoRepository.findByEstaEnCartaTrue()).thenReturn(enCarta);

        List<Plato> resultado = platoService.listarCarta();

        assertEquals(enCarta, resultado);
        verify(platoRepository).findByEstaEnCartaTrue();
    }

    @Test
    @DisplayName("CPU-04.2 / CPU-12.2 / CPU-15.2: listarCartaPorCategoria filtra por la categoría pedida")
    void listarCartaPorCategoria_filtraPorCategoria() {

        List<Plato> entrantes = List.of(platoEnCarta);
        when(platoRepository.findByEstaEnCartaTrueAndCategoria(CategoriaPlato.PRINCIPAL))
                .thenReturn(entrantes);

        List<Plato> resultado = platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL);

        assertEquals(entrantes, resultado);
        verify(platoRepository)
                .findByEstaEnCartaTrueAndCategoria(CategoriaPlato.PRINCIPAL);
    }

    @Test
    @DisplayName("CPU-04.3: buscarPorId devuelve el plato si existe")
    void buscarPorId_existe_devuelvePlato() {

        when(platoRepository.findById(3L)).thenReturn(Optional.of(platoEnCarta));

        Plato resultado = platoService.buscarPorId(3L);

        assertSame(platoEnCarta, resultado);
    }

    @Test
    @DisplayName("CPU-04.4: buscarPorId lanza RecursoNoEncontrado si no existe")
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {

        when(platoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> platoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("CPU-04.5: listarTodosPlatos devuelve todos los platos del repositorio")
    void listarTodosPlatos_devuelveTodos() {

        List<Plato> todos = List.of(platoEnCarta, platoDelChef);
        when(platoRepository.findAll()).thenReturn(todos);

        assertEquals(todos, platoService.listarTodosPlatos());
        verify(platoRepository).findAll();
    }

    @Test
    @DisplayName("CPU-17.1: Plato válido se guarda en la carta y no es del chef")
    void anadirPlatoALaCarta_valido_guardaPlatoEnCarta() {

        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoService.anadirPlatoALaCarta(dtoValido);

        ArgumentCaptor<Plato> captor = ArgumentCaptor.forClass(Plato.class);
        verify(platoRepository).save(captor.capture());
        Plato guardado = captor.getValue();

        assertSame(guardado, resultado);
        assertEquals("Plato 5", guardado.getNombre());
        assertEquals("Descripción 5", guardado.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(guardado.getPrecio()));
        assertEquals(CategoriaPlato.ENTRANTE, guardado.getCategoria());
        assertTrue(guardado.getEstaEnCarta());
        assertFalse(guardado.getEsDelChef());
    }

    @Test
    @DisplayName("CPU-17.2: Quita los espacios del nombre y la descripción y deja el precio con 2 decimales")
    void anadirPlatoALaCarta_limpiaDatosYRedondeaPrecio() {

        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatoCreacionDTO conEspacios = new PlatoCreacionDTO();
        conEspacios.setNombre("  Plato 6  ");
        conEspacios.setDescripcion("  Descripción 6  ");
        conEspacios.setPrecio(new BigDecimal("12.345"));
        conEspacios.setCategoria(CategoriaPlato.PRINCIPAL);

        Plato resultado = platoService.anadirPlatoALaCarta(conEspacios);

        assertEquals("Plato 6", resultado.getNombre());
        assertEquals("Descripción 6", resultado.getDescripcion());
        assertEquals(new BigDecimal("12.35"), resultado.getPrecio());
    }

    @Test
    @DisplayName("CPU-17.3: Rechaza el plato si el precio es 0")
    void anadirPlatoALaCarta_precioCero_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 7", "Descripción 7", "0", CategoriaPlato.BEBIDA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("precio"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.4: Rechaza el plato si el precio es negativo")
    void anadirPlatoALaCarta_precioNegativo_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 7", "Descripción 7", "-3.00", CategoriaPlato.BEBIDA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("precio"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.5: Rechaza el plato si el nombre está vacío")
    void anadirPlatoALaCarta_nombreVacio_lanzaError() {

        PlatoCreacionDTO dto = dto("   ", "Descripción", "5.00", CategoriaPlato.POSTRE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("nombre"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.6: Rechaza el plato si la descripción está vacía")
    void anadirPlatoALaCarta_descripcionVacia_lanzaError() {
        PlatoCreacionDTO dto = dto("Plato 8", "", "5.00", CategoriaPlato.POSTRE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("descripcion"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.7: Rechaza el plato si no tiene categoría")
    void anadirPlatoALaCarta_sinCategoria_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 8", "Descripción 8", "5.00", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("categoria"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.8: Rechaza el plato si el nombre ya existe")
    void anadirPlatoALaCarta_nombreDuplicado_lanzaError() {

        when(platoRepository.findAll()).thenReturn(List.of(platoEnCarta));

        PlatoCreacionDTO dto = dto("Plato 3", "Descripción 3", "10.00", CategoriaPlato.ENTRANTE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("ya existe"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.9: Rechaza la petición si no llegan datos")
    void anadirPlatoALaCarta_dtoNulo_lanzaError() {

        assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(null));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.10: Un plato retirado vuelve a estar en la carta")
    void anadirPlatoExistente_retirado_vuelveALaCarta() {

        when(platoRepository.findById(1L)).thenReturn(Optional.of(platoRetirado));
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoService.anadirPlatoExistenteALaCarta(1L);

        assertTrue(resultado.getEstaEnCarta());
        assertFalse(resultado.getEsDelChef());
        verify(platoRepository).save(resultado);
    }

    @Test
    @DisplayName("CPU-17.11: Rechaza añadir a la carta un plato del chef")
    void anadirPlatoExistente_delChef_lanzaOperacionNoPermitida() {

        when(platoRepository.findById(2L)).thenReturn(Optional.of(platoDelChef));

        assertThrows(OperacionNoPermitidaException.class, () -> platoService.anadirPlatoExistenteALaCarta(2L));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.12: Rechaza añadir un plato que ya está en la carta")
    void anadirPlatoExistente_yaEnCarta_lanzaError() {

        when(platoRepository.findById(3L)).thenReturn(Optional.of(platoEnCarta));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoExistenteALaCarta(3L));
        assertTrue(ex.getMessage().contains("ya está en la carta"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.13: Lanza RecursoNoEncontrado si el plato no existe")
    void anadirPlatoExistente_inexistente_lanzaRecursoNoEncontrado() {

        when(platoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> platoService.anadirPlatoExistenteALaCarta(99L));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.14: La lista de retirados usa la consulta que excluye los platos del chef")
    void listarPlatosRetirados_devuelveLosDelRepositorio() {

        List<Plato> retirados = List.of(platoRetirado);
        when(platoRepository.findByEstaEnCartaFalseAndEsDelChefFalse()).thenReturn(retirados);

        assertEquals(retirados, platoService.listarPlatosRetiradosDeLaCarta());
    }
}
