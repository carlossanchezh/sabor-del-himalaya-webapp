package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;
import es.upm.is2.sabordelhimalaya.dto.response.ResumenPedidoDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.enums.EstadoPedido;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.LineaPedidoMenuDiario;
import es.upm.is2.sabordelhimalaya.model.LineaPedidoPlato;
import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;
import es.upm.is2.sabordelhimalaya.model.Pedido;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.repository.LineaPedidoMenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.LineaPedidoPlatoRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioOpcionRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.PedidoRepository;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;
import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-06 (Crear pedido)
 - US-07 (Revisar y confirmar pedido)
 - US-08 (Consultar el estado del pedido)
 */

@ExtendWith(MockitoExtension.class)
public class PedidoServiceImplTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private MenuDiarioRepository menuDiarioRepository;
    @Mock private MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    @Mock private LineaPedidoPlatoRepository lineaPedidoPlatoRepository;
    @Mock private LineaPedidoMenuDiarioRepository lineaPedidoMenuDiarioRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario usuario;
    private Plato entrante, principal, postre, bebida;
    private Plato platoRetirado;
    private MenuDiario menuHoy;
    private MenuDiario menuAyer;
    private LineaPedidoPlatoDTO lineaPlatoDTO;
    private LineaPedidoMenuDiarioDTO lineaMenuDTO;

    @BeforeEach
    void setUp() {

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Usuario 1");
        usuario.setDireccion("Calle Falsa 123");
        usuario.setLogin("usuario1");

        entrante       = plato(10L, CategoriaPlato.ENTRANTE,  true);
        principal      = plato(11L, CategoriaPlato.PRINCIPAL, true);
        postre         = plato(12L, CategoriaPlato.POSTRE,     true);
        bebida         = plato(13L, CategoriaPlato.BEBIDA,     true);
        platoRetirado  = plato(20L, CategoriaPlato.PRINCIPAL,  false);

        menuHoy    = new MenuDiario(1L, LocalDate.now(),new BigDecimal("15.00"));
        menuAyer = new MenuDiario(2L, LocalDate.now().minusDays(1), new BigDecimal("15.00"));

        lineaPlatoDTO = new LineaPedidoPlatoDTO();
        lineaPlatoDTO.setIdPlato(entrante.getIdPlato());
        lineaPlatoDTO.setCantidad(1);

        lineaMenuDTO = new LineaPedidoMenuDiarioDTO();
        lineaMenuDTO.setIdMenuDiario(menuHoy.getIdMenuDiario());
        lineaMenuDTO.setIdEntrante(entrante.getIdPlato());
        lineaMenuDTO.setIdPrincipal(principal.getIdPlato());
        lineaMenuDTO.setIdPostre(postre.getIdPlato());
        lineaMenuDTO.setIdBebida(bebida.getIdPlato());
        lineaMenuDTO.setCantidad(1);
    }

    private Plato plato(long id, CategoriaPlato cat, boolean enCarta) {
        return new Plato(id, "Plato " + id, "Descripción " + id, new BigDecimal("10.00"), cat, enCarta, false);
    }

    private void prepararMenuHoyConOpciones() {

        when(menuDiarioRepository.findById(1L)).thenReturn(Optional.of(menuHoy));
        when(platoRepository.findById(13L)).thenReturn(Optional.of(bebida));

        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.ENTRANTE))
                .thenReturn(List.of(opcion(menuHoy, entrante, CategoriaMenu.ENTRANTE)));

        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.PRINCIPAL))
                .thenReturn(List.of(opcion(menuHoy, principal, CategoriaMenu.PRINCIPAL)));

        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.POSTRE))
                .thenReturn(List.of(opcion(menuHoy, postre, CategoriaMenu.POSTRE)));
    }

    private MenuDiarioOpcion opcion(MenuDiario menu, Plato plato, CategoriaMenu categoria) {
        return new MenuDiarioOpcion(null, menu, plato, categoria);
    }

    @Test
    @DisplayName("CPU-07.1: crearPedido válido con un plato guarda con total y estado correctos")
    void crearPedido_unPlato_guardaCorrectamente() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.crearPedido(1L, "Calle Mayor 5",
                List.of(lineaPlatoDTO), List.of());

        assertEquals(0, new BigDecimal("10.00").compareTo(resultado.getImporteTotal()));
        assertEquals(EstadoPedido.RECIBIDO, resultado.getEstado());
        assertEquals("Calle Mayor 5", resultado.getDireccionEntrega());
        assertSame(usuario, resultado.getUsuario());
        assertNotNull(resultado.getFechaHora());

        verify(pedidoRepository).save(any(Pedido.class));
        verify(lineaPedidoPlatoRepository).saveAll(any());
        verify(lineaPedidoMenuDiarioRepository).saveAll(any());
    }

    @Test
    @DisplayName("CPU-07.2: crearPedido válido con un menú del día guarda con total 15 €")
    void crearPedido_unMenu_guardaConTotalDelMenu() {

        prepararMenuHoyConOpciones();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.crearPedido(1L, "Calle Mayor 5",
                List.of(), List.of(lineaMenuDTO));

        assertEquals(0, new BigDecimal("15.00").compareTo(resultado.getImporteTotal()));
        verify(lineaPedidoMenuDiarioRepository).saveAll(any());
    }

    @Test
    @DisplayName("CPU-07.3: crearPedido vacío lanza IllegalArgumentException")
    void crearPedido_vacio_lanzaError() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "Calle Mayor 5", List.of(), List.of()));

        assertTrue(ex.getMessage().contains("vacío"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-07.4: crearPedido sin dirección lanza IllegalArgumentException")
    void crearPedido_sinDireccion_lanzaError() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "   ", List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("dirección"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-07.5: crearPedido con usuario inexistente lanza RecursoNoEncontrado")
    void crearPedido_usuarioInexistente_lanzaRecursoNoEncontrado() {

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearPedido(99L, "Calle Mayor 5",
                        List.of(lineaPlatoDTO), List.of()));

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-07.6: crearPedido con un plato retirado de la carta lanza IllegalArgumentException")
    void crearPedido_platoRetirado_lanzaError() {

        lineaPlatoDTO.setIdPlato(platoRetirado.getIdPlato());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(platoRepository.findById(20L)).thenReturn(Optional.of(platoRetirado));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "Calle Mayor 5",
                        List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("ya no está en la carta"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-07.7: crearPedido con cantidad 0 lanza IllegalArgumentException")
    void crearPedido_cantidadCero_lanzaError() {

        lineaPlatoDTO.setCantidad(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "Calle Mayor 5",
                        List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("cantidad"));
    }

    @Test
    @DisplayName("CPU-07.8: crearPedido con menú que no es de hoy lanza IllegalArgumentException")
    void crearPedido_menuNoEsDeHoy_lanzaError() {

        lineaMenuDTO.setIdMenuDiario(menuAyer.getIdMenuDiario());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(menuDiarioRepository.findById(2L)).thenReturn(Optional.of(menuAyer));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "Calle Mayor 5",
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().contains("hoy"));
    }

    @Test
    @DisplayName("CPU-07.9: resumirPedidoTemporal con un plato devuelve 1 línea y total correcto")
    void resumirPedidoTemporal_conPlato_devuelveResumen() {

        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(lineaPlatoDTO), List.of());

        assertEquals(1, resumen.getLineas().size());
        assertEquals(0, new BigDecimal("10.00").compareTo(resumen.getTotal()));

        ResumenPedidoDTO.Linea linea = resumen.getLineas().get(0);
        assertFalse(linea.isMenu());
        assertEquals("Plato 10", linea.getNombre());
        assertNull(linea.getDetalle());
    }

    @Test
    @DisplayName("CPU-07.10: resumirPedidoTemporal con un menú devuelve detalle con los 4 platos")
    void resumirPedidoTemporal_conMenu_devuelveDetalle() {

        prepararMenuHoyConOpciones();

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(), List.of(lineaMenuDTO));

        assertEquals(1, resumen.getLineas().size());
        assertEquals(0, new BigDecimal("15.00").compareTo(resumen.getTotal()));

        ResumenPedidoDTO.Linea linea = resumen.getLineas().get(0);
        assertTrue(linea.isMenu());
        assertEquals("Menú del día", linea.getNombre());
        // El detalle concatena entrante · principal · postre · bebida
        assertEquals("Plato 10 · Plato 11 · Plato 12 · Plato 13", linea.getDetalle());
    }

    @Test
    @DisplayName("CPU-07.11: resumirPedidoTemporal con listas vacías devuelve resumen vacío")
    void resumirPedidoTemporal_vacio_devuelveVacio() {

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(List.of(), List.of());

        assertTrue(resumen.getLineas().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(resumen.getTotal()));
    }

    @Test
    @DisplayName("CPU-08.1: obtenerResumenDePedido devuelve las líneas guardadas y el importe")
    void obtenerResumenDePedido_devuelveLineas() {

        Pedido pedido = new Pedido();
        pedido.setIdPedido(5L);
        pedido.setImporteTotal(new BigDecimal("25.00"));

        LineaPedidoPlato lp = new LineaPedidoPlato();
        lp.setPlato(entrante);
        lp.setCantidad(1);
        lp.setPrecioUnitario(new BigDecimal("10.00"));

        LineaPedidoMenuDiario lm = new LineaPedidoMenuDiario();
        lm.setMenuDiario(menuHoy);
        lm.setEntrante(entrante);
        lm.setPrincipal(principal);
        lm.setPostre(postre);
        lm.setBebida(bebida);
        lm.setCantidad(1);
        lm.setPrecioUnitario(new BigDecimal("15.00"));

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(lineaPedidoPlatoRepository.findByPedidoIdPedido(5L)).thenReturn(List.of(lp));
        when(lineaPedidoMenuDiarioRepository.findByPedidoIdPedido(5L)).thenReturn(List.of(lm));

        ResumenPedidoDTO resumen = pedidoService.obtenerResumenDePedido(5L);

        assertEquals(2, resumen.getLineas().size());
        assertEquals(0, new BigDecimal("25.00").compareTo(resumen.getTotal()));
    }

    @Test
    @DisplayName("CPU-08.2: obtenerResumenDePedido con pedido inexistente lanza RecursoNoEncontrado")
    void obtenerResumenDePedido_noExiste_lanzaRecursoNoEncontrado() {

        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.obtenerResumenDePedido(99L));
    }

    @Test
    @DisplayName("CPU-08.3: listarPedidosDeUsuario devuelve los pedidos del repositorio")
    void listarPedidosDeUsuario_devuelveLosDelRepositorio() {

        List<Pedido> pedidos = List.of(new Pedido(), new Pedido());
        when(pedidoRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(1L))
                .thenReturn(pedidos);

        assertEquals(pedidos, pedidoService.listarPedidosDeUsuario(1L));
        verify(pedidoRepository).findByUsuarioIdUsuarioOrderByFechaHoraDesc(1L);
    }

    @Test
    @DisplayName("CPU-08.4: buscarPorId devuelve el pedido si existe")
    void buscarPorId_existe_devuelvePedido() {

        Pedido pedido = new Pedido();
        pedido.setIdPedido(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertSame(pedido, pedidoService.buscarPorId(1L));
    }

    @Test
    @DisplayName("CPU-08.5: buscarPorId lanza RecursoNoEncontrado si no existe")
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {

        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.buscarPorId(99L));
    }
}
