package es.upm.is2.sabordelhimalaya.dto.session;

import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-06 (Añadir productos al pedido temporal de la sesión)
 */

class PedidoTemporalDTOTest {

    private PedidoTemporalDTO pedidoTemporal;

    private LineaPedidoPlatoDTO lineaPlato;
    private LineaPedidoMenuDiarioDTO lineaMenu;

    @BeforeEach
    void setUp() {

        pedidoTemporal = new PedidoTemporalDTO();

        lineaPlato = lineaPlato(10L, 1);
        lineaMenu  = lineaMenu(1L, 10L, 11L, 12L, 13L, 1);
    }


    private LineaPedidoPlatoDTO lineaPlato(long idPlato, int cantidad) {

        LineaPedidoPlatoDTO l = new LineaPedidoPlatoDTO();
        l.setIdPlato(idPlato);
        l.setCantidad(cantidad);
        return l;
    }

    private LineaPedidoMenuDiarioDTO lineaMenu(long idMenu, long idEntrante, long idPrincipal, long idPostre, long idBebida, int cantidad) {

        LineaPedidoMenuDiarioDTO l = new LineaPedidoMenuDiarioDTO();
        l.setIdMenuDiario(idMenu);
        l.setIdEntrante(idEntrante);
        l.setIdPrincipal(idPrincipal);
        l.setIdPostre(idPostre);
        l.setIdBebida(idBebida);
        l.setCantidad(cantidad);
        return l;
    }

    @Test
    @DisplayName("PU-06-01: Añadir un plato nuevo al carrito crea una línea")
    void anadirPlato_platoNuevo_agregaLinea() {

        pedidoTemporal.anadirPlato(lineaPlato);

        assertEquals(1, pedidoTemporal.getLineasPlato().size());
        assertEquals(10L, pedidoTemporal.getLineasPlato().getFirst().getIdPlato());
        assertEquals(1, pedidoTemporal.getLineasPlato().getFirst().getCantidad());
        assertTrue(pedidoTemporal.getLineasMenu().isEmpty());
    }

    @Test
    @DisplayName("PU-06-02: Añadir el mismo plato otra vez suma la cantidad")
    void anadirPlato_mismoPlato_sumaCantidad() {

        pedidoTemporal.anadirPlato(lineaPlato(10L, 1));
        pedidoTemporal.anadirPlato(lineaPlato(10L, 2));

        assertEquals(1, pedidoTemporal.getLineasPlato().size());
        assertEquals(3, pedidoTemporal.getLineasPlato().getFirst().getCantidad());
    }

    @Test
    @DisplayName("PU-06-03: Añadir un plato distinto crea una segunda línea")
    void anadirPlato_distintoPlato_agregaSegundaLinea() {

        pedidoTemporal.anadirPlato(lineaPlato(10L, 1));
        pedidoTemporal.anadirPlato(lineaPlato(11L, 1));

        assertEquals(2, pedidoTemporal.getLineasPlato().size());
        assertEquals(10L, pedidoTemporal.getLineasPlato().get(0).getIdPlato());
        assertEquals(11L, pedidoTemporal.getLineasPlato().get(1).getIdPlato());
    }

    @Test
    @DisplayName("PU-06-04: Añadir un menú con una selección nueva crea una línea")
    void anadirMenu_nuevaSeleccion_agregaLinea() {

        pedidoTemporal.anadirMenu(lineaMenu);

        assertEquals(1, pedidoTemporal.getLineasMenu().size());
        assertEquals(1, pedidoTemporal.getLineasMenu().getFirst().getCantidad());
        assertTrue(pedidoTemporal.getLineasPlato().isEmpty());
    }

    @Test
    @DisplayName("PU-06-05: Añadir el mismo menú con la misma selección suma la cantidad")
    void anadirMenu_mismaSeleccion_sumaCantidad() {

        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 1));
        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 2));

        assertEquals(1, pedidoTemporal.getLineasMenu().size());
        assertEquals(3, pedidoTemporal.getLineasMenu().getFirst().getCantidad());
    }

    @Test
    @DisplayName("PU-06-06-01: Elegir otro entrante crea una segunda línea de menú")
    void anadirMenu_distintoEntrante_agregaSegundaLinea() {

        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 1));
        pedidoTemporal.anadirMenu(lineaMenu(1L, 99L, 11L, 12L, 13L, 1));

        assertEquals(2, pedidoTemporal.getLineasMenu().size());
    }

    @Test
    @DisplayName("PU-06-06-02: Elegir otro principal crea una segunda línea de menú")
    void anadirMenu_distintoPrincipal_agregaSegundaLinea() {

        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 1));
        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 99L, 12L, 13L, 1));

        assertEquals(2, pedidoTemporal.getLineasMenu().size());
    }

    @Test
    @DisplayName("PU-06-06-03: Elegir otro postre crea una segunda línea de menú")
    void anadirMenu_distintoPostre_agregaSegundaLinea() {

        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 1));
        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 99L, 13L, 1));

        assertEquals(2, pedidoTemporal.getLineasMenu().size());
    }

    @Test
    @DisplayName("PU-06-06-04: Elegir otra bebida crea una segunda línea de menú")
    void anadirMenu_distintaBebida_agregaSegundaLinea() {

        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 13L, 1));
        pedidoTemporal.anadirMenu(lineaMenu(1L, 10L, 11L, 12L, 99L, 1));

        assertEquals(2, pedidoTemporal.getLineasMenu().size());
    }

    @Test
    @DisplayName("PU-06-07: Un carrito recién creado está vacío")
    void estaVacio_recienCreado_devuelveTrue() {

        assertTrue(pedidoTemporal.estaVacio());
    }

    @Test
    @DisplayName("PU-06-08: Un carrito con un plato no está vacío")
    void estaVacio_conPlato_devuelveFalse() {

        pedidoTemporal.anadirPlato(lineaPlato);

        assertFalse(pedidoTemporal.estaVacio());
    }

    @Test
    @DisplayName("PU-06-09: Un carrito con un menú no está vacío")
    void estaVacio_conMenu_devuelveFalse() {

        pedidoTemporal.anadirMenu(lineaMenu);

        assertFalse(pedidoTemporal.estaVacio());
    }

    @Test
    @DisplayName("PU-06-10: Vaciar el carrito elimina platos y menús")
    void vaciar_eliminaTodoElContenido() {

        pedidoTemporal.anadirPlato(lineaPlato);
        pedidoTemporal.anadirMenu(lineaMenu);

        assertFalse(pedidoTemporal.estaVacio());

        pedidoTemporal.vaciar();

        assertTrue(pedidoTemporal.estaVacio());
        assertTrue(pedidoTemporal.getLineasPlato().isEmpty());
        assertTrue(pedidoTemporal.getLineasMenu().isEmpty());
    }

    @Test
    @DisplayName("PU-06-11: Si la sesión no tiene carrito, se crea uno vacío y se guarda")
    void deSesion_sinAtributo_creaVacioYLoGuarda() {

        HttpSession session = mock(HttpSession.class);
        Map<String, Object> store = new HashMap<>();

        when(session.getAttribute("pedidoTemporal"))
                .thenAnswer(inv -> store.get(inv.getArgument(0)));
        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());

        PedidoTemporalDTO resultado = PedidoTemporalDTO.deSesion(session);

        assertNotNull(resultado);
        assertTrue(resultado.estaVacio());
        assertSame(resultado, store.get("pedidoTemporal"));
        verify(session).setAttribute(eq("pedidoTemporal"), same(resultado));
    }

    @Test
    @DisplayName("PU-06-12: Si la sesión ya tiene carrito, se devuelve el mismo")
    void deSesion_conAtributo_devuelveElMismo() {

        HttpSession session = mock(HttpSession.class);
        PedidoTemporalDTO existente = new PedidoTemporalDTO();

        when(session.getAttribute("pedidoTemporal")).thenReturn(existente);

        PedidoTemporalDTO resultado = PedidoTemporalDTO.deSesion(session);

        assertSame(existente, resultado);
        verify(session, never()).setAttribute(anyString(), any());
    }
}