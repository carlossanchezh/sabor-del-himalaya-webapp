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

    private Plato entranteEnCarta;
    private Plato principalEnCarta;
    private Plato postreEnCarta;
    private Plato bebidaEnCarta;

    private Plato entranteRetirado;
    private Plato principalRetirado;
    private Plato postreRetirado;
    private Plato bebidaRetirado;

    private Plato entranteDelChef;
    private Plato principalDelChef;
    private Plato postreDelChef;
    private Plato bebidaDelChef;

    private PlatoCreacionDTO dtoValido;

    @BeforeEach
    void setUp() {

        entranteEnCarta  = plato(10L, "Entrante Carta",  CategoriaPlato.ENTRANTE,  true,  false);
        principalEnCarta = plato(11L, "Principal Carta", CategoriaPlato.PRINCIPAL, true,  false);
        postreEnCarta    = plato(12L, "Postre Carta",    CategoriaPlato.POSTRE,    true,  false);
        bebidaEnCarta    = plato(13L, "Bebida Carta",    CategoriaPlato.BEBIDA,    true,  false);

        entranteRetirado  = plato(20L, "Entrante Retirado",  CategoriaPlato.ENTRANTE,  false, false);
        principalRetirado = plato(21L, "Principal Retirado", CategoriaPlato.PRINCIPAL, false, false);
        postreRetirado    = plato(22L, "Postre Retirado",    CategoriaPlato.POSTRE,    false, false);
        bebidaRetirado    = plato(23L, "Bebida Retirada",    CategoriaPlato.BEBIDA,    false, false);

        entranteDelChef  = plato(30L, "Entrante del Chef",  CategoriaPlato.ENTRANTE,  false, true);
        principalDelChef = plato(31L, "Principal del Chef", CategoriaPlato.PRINCIPAL, false, true);
        postreDelChef    = plato(32L, "Postre del Chef",    CategoriaPlato.POSTRE,    false, true);
        bebidaDelChef    = plato(33L, "Bebida del Chef",    CategoriaPlato.BEBIDA,    false, true);

        dtoValido = new PlatoCreacionDTO();
        dtoValido.setNombre("Plato 5");
        dtoValido.setDescripcion("Descripción 5");
        dtoValido.setPrecio(new BigDecimal("10.00"));
        dtoValido.setCategoria(CategoriaPlato.ENTRANTE);

    }

    private Plato plato(long id, String nombre, CategoriaPlato cat, boolean enCarta, boolean delChef) {

        return new Plato(id, nombre, "Descripción " + id, new BigDecimal("10.00"), cat, enCarta, delChef);
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
    @DisplayName("PU-04-01 / PU-12-01 / PU-15-01: listarCarta devuelve solo los platos visibles en carta")
    void listarCarta_devuelveLosPlatosDeLaCarta() {

        List<Plato> enCarta = List.of(entranteEnCarta, principalEnCarta, postreEnCarta, bebidaEnCarta);
        when(platoRepository.findByEstaEnCartaTrue()).thenReturn(enCarta);

        List<Plato> resultado = platoService.listarCarta();

        assertEquals(enCarta, resultado);
        verify(platoRepository).findByEstaEnCartaTrue();
    }

    @Test
    @DisplayName("PU-04-02 / PU-12-02 / PU-15-02: listarCartaPorCategoria filtra por la categoría pedida")
    void listarCartaPorCategoria_filtraPorCategoria_PrincipalEntrante() {

        when(platoRepository.findByEstaEnCartaTrueAndCategoria(CategoriaPlato.ENTRANTE))
                .thenReturn(List.of(entranteEnCarta));

        List<Plato> resultado = platoService.listarCartaPorCategoria(CategoriaPlato.ENTRANTE);

        assertEquals(List.of(entranteEnCarta), resultado);
        verify(platoRepository)
                .findByEstaEnCartaTrueAndCategoria(CategoriaPlato.ENTRANTE);
    }

    @Test
    @DisplayName("PU-04-03 / PU-12-03 / PU-15-03: listarCartaPorCategoria filtra por la categoría pedida")
    void listarCartaPorCategoria_filtraPorCategoria_Principal() {

        when(platoRepository.findByEstaEnCartaTrueAndCategoria(CategoriaPlato.PRINCIPAL))
                .thenReturn(List.of(principalEnCarta));

        List<Plato> resultado = platoService.listarCartaPorCategoria(CategoriaPlato.PRINCIPAL);

        assertEquals(List.of(principalEnCarta), resultado);
        verify(platoRepository)
                .findByEstaEnCartaTrueAndCategoria(CategoriaPlato.PRINCIPAL);
    }

    @Test
    @DisplayName("PU-04-04 / PU-12-04 / PU-15-04: listarCartaPorCategoria filtra por la categoría pedida")
    void listarCartaPorCategoria_filtraPorCategoria_Postre() {

        when(platoRepository.findByEstaEnCartaTrueAndCategoria(CategoriaPlato.POSTRE))
                .thenReturn(List.of(postreEnCarta));

        List<Plato> resultado = platoService.listarCartaPorCategoria(CategoriaPlato.POSTRE);

        assertEquals(List.of(postreEnCarta), resultado);
        verify(platoRepository)
                .findByEstaEnCartaTrueAndCategoria(CategoriaPlato.POSTRE);
    }

    @Test
    @DisplayName("PU-04-05 / PU-12-05 / PU-15-05: listarCartaPorCategoria filtra por la categoría pedida")
    void listarCartaPorCategoria_filtraPorCategoria_Bebida() {

        when(platoRepository.findByEstaEnCartaTrueAndCategoria(CategoriaPlato.BEBIDA))
                .thenReturn(List.of(bebidaEnCarta));

        List<Plato> resultado = platoService.listarCartaPorCategoria(CategoriaPlato.BEBIDA);

        assertEquals(List.of(bebidaEnCarta), resultado);
        verify(platoRepository)
                .findByEstaEnCartaTrueAndCategoria(CategoriaPlato.BEBIDA);
    }

    @Test
    @DisplayName("PU-04-06 / PU-12-06 : listarCarta devuelve los platos con sus datos completos")
    void listarCarta_devuelvePlatosConSusDatos() {

        when(platoRepository.findByEstaEnCartaTrue())
                .thenReturn(List.of(entranteEnCarta, principalEnCarta, postreEnCarta, bebidaEnCarta));

        List<Plato> resultado = platoService.listarCarta();

        assertEquals(4, resultado.size());

        Plato primero = resultado.getFirst();
        assertEquals("Entrante Carta", primero.getNombre());
        assertEquals("Descripción 10", primero.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(primero.getPrecio()));
        assertEquals(CategoriaPlato.ENTRANTE, primero.getCategoria());

        Plato segundo = resultado.get(1);
        assertEquals("Principal Carta", segundo.getNombre());
        assertEquals("Descripción 11", segundo.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(segundo.getPrecio()));
        assertEquals(CategoriaPlato.PRINCIPAL, segundo.getCategoria());

        Plato tercero = resultado.get(2);
        assertEquals("Postre Carta", tercero.getNombre());
        assertEquals("Descripción 12", tercero.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(tercero.getPrecio()));
        assertEquals(CategoriaPlato.POSTRE, tercero.getCategoria());

        Plato cuarto = resultado.get(3);
        assertEquals("Bebida Carta", cuarto.getNombre());
        assertEquals("Descripción 13", cuarto.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(cuarto.getPrecio()));
        assertEquals(CategoriaPlato.BEBIDA, cuarto.getCategoria());
    }

    @Test
    @DisplayName("PU-04-07 / PU-12-07: buscarPorId devuelve nombre, descripción y precio del plato")
    void buscarPorId_devuelveDatosDelPlato() {

        when(platoRepository.findById(11L)).thenReturn(Optional.of(principalEnCarta));

        Plato resultado = platoService.buscarPorId(11L);

        assertEquals("Principal Carta", resultado.getNombre());
        assertEquals("Descripción 11", resultado.getDescripcion());
        assertEquals(0, new BigDecimal("10.00").compareTo(resultado.getPrecio()));
        assertEquals(CategoriaPlato.PRINCIPAL, resultado.getCategoria());
    }

    @Test
    @DisplayName("PU-04-08 / PU-12-08 / PU-15-06: buscarPorId lanza RecursoNoEncontrado si no existe")
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {

        when(platoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> platoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("PU-04-09 / PU-12-09 / PU-15-07: listarTodosPlatos devuelve todos los platos del repositorio")
    void listarTodosPlatos_devuelveTodos() {

        List<Plato> todos = List.of(entranteEnCarta, entranteRetirado, entranteDelChef);
        when(platoRepository.findAll()).thenReturn(todos);

        assertEquals(todos, platoService.listarTodosPlatos());
        verify(platoRepository).findAll();
    }

    @Test
    @DisplayName("PU-17-01: Plato válido se guarda en la carta y no es del chef")
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
    @DisplayName("PU-17-02: Quita los espacios del nombre y la descripción y deja el precio con 2 decimales")
    void anadirPlatoALaCarta_limpiaDatosYRedondeaPrecio() {

        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatoCreacionDTO conEspacios = dto("  Plato 6  ", "  Descripción 6  ", "12.345", CategoriaPlato.PRINCIPAL);

        Plato resultado = platoService.anadirPlatoALaCarta(conEspacios);

        assertEquals("Plato 6", resultado.getNombre());
        assertEquals("Descripción 6", resultado.getDescripcion());
        assertEquals(new BigDecimal("12.35"), resultado.getPrecio());
    }

    @Test
    @DisplayName("PU-17-03-01: Rechaza el plato si el nombre está vacío")
    void anadirPlatoALaCarta_nombreVacio_lanzaError() {

        PlatoCreacionDTO dto = dto("   ", "Descripción", "5.00", CategoriaPlato.POSTRE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("nombre"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-03-02: Rechaza el plato si la descripción está vacía")
    void anadirPlatoALaCarta_descripcionVacia_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 8", "", "5.00", CategoriaPlato.POSTRE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("descripcion"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-03-03: Rechaza el plato si no tiene categoría")
    void anadirPlatoALaCarta_sinCategoria_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 8", "Descripción 8", "5.00", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("categoria"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-04-01: Rechaza el plato si el precio es 0")
    void anadirPlatoALaCarta_precioCero_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 7", "Descripción 7", "0", CategoriaPlato.BEBIDA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("precio"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-04-02: Rechaza el plato si el precio es negativo")
    void anadirPlatoALaCarta_precioNegativo_lanzaError() {

        PlatoCreacionDTO dto = dto("Plato 7", "Descripción 7", "-3.00", CategoriaPlato.BEBIDA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("precio"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-05-01: Rechaza el plato si el nombre ya existe")
    void anadirPlatoALaCarta_nombreDuplicado_lanzaError() {

        when(platoRepository.existsByNombreIgnoreCaseAndEsDelChefFalse("Principal Carta"))
                .thenReturn(true);

        PlatoCreacionDTO dto = dto("Principal Carta", "Otra descripción", "10.00", CategoriaPlato.PRINCIPAL);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> platoService.anadirPlatoALaCarta(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("ya existe"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-05-02: Rechaza plato con nombre duplicado ignorando espacios y mayúsculas")
    void anadirPlatoALaCarta_nombreDuplicadoIgnorandoEspacios_lanzaError() {

        when(platoRepository.existsByNombreIgnoreCaseAndEsDelChefFalse("Principal Carta"))
                .thenReturn(true);

        PlatoCreacionDTO dto = dto("  Principal Carta  ", "otra", "10.00", CategoriaPlato.PRINCIPAL);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> platoService.anadirPlatoALaCarta(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("ya existe"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-05-03: Permite el nombre de un plato del chef")
    void anadirPlatoALaCarta_nombreIgualAPlatoDelChef_permiteCrear() {

        when(platoRepository.existsByNombreIgnoreCaseAndEsDelChefFalse("Postre del Chef"))
                .thenReturn(false);
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatoCreacionDTO dto = dto("Postre del Chef", "Descripción 32", "10.00", CategoriaPlato.POSTRE);

        Plato resultado = platoService.anadirPlatoALaCarta(dto);

        assertEquals("Postre del Chef", resultado.getNombre());
        assertTrue(resultado.getEstaEnCarta());
        assertFalse(resultado.getEsDelChef());
        verify(platoRepository).save(any(Plato.class));
    }

    @Test
    @DisplayName("PU-17-06: Un plato retirado vuelve a estar en la carta")
    void anadirPlatoExistente_retirado_vuelveALaCarta() {

        when(platoRepository.findById(21L)).thenReturn(Optional.of(principalRetirado));
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoService.anadirPlatoExistenteALaCarta(21L);

        assertTrue(resultado.getEstaEnCarta());
        assertFalse(resultado.getEsDelChef());
        verify(platoRepository).save(resultado);
    }

    @Test
    @DisplayName("PU-17-07: Rechaza añadir a la carta un plato del chef")
    void anadirPlatoExistente_delChef_lanzaOperacionNoPermitida() {

        when(platoRepository.findById(31L)).thenReturn(Optional.of(principalDelChef));

        assertThrows(OperacionNoPermitidaException.class,
                () -> platoService.anadirPlatoExistenteALaCarta(31L));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-08: Rechaza añadir un plato que ya está en la carta")
    void anadirPlatoExistente_yaEnCarta_lanzaError() {

        when(platoRepository.findById(11L)).thenReturn(Optional.of(principalEnCarta));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> platoService.anadirPlatoExistenteALaCarta(11L));
        assertTrue(ex.getMessage().contains("ya está en la carta"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-09: Rechaza la petición si no llegan datos")
    void anadirPlatoALaCarta_dtoNulo_lanzaError() {

        assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(null));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-17-10: listarPlatosRetirados devuelve solo los platos inactivos que no son del chef")
    void listarPlatosRetirados_devuelveLosDelRepositorio() {

        List<Plato> retirados = List.of(entranteRetirado, principalRetirado, postreRetirado, bebidaRetirado);
        when(platoRepository.findByEstaEnCartaFalseAndEsDelChefFalse()).thenReturn(retirados);

        List<Plato> resultado = platoService.listarPlatosRetiradosDeLaCarta();

        assertEquals(retirados, resultado);
        verify(platoRepository).findByEstaEnCartaFalseAndEsDelChefFalse();
    }
}
