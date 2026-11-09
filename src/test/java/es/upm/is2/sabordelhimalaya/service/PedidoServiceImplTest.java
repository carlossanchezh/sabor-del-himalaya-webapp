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

    private Pedido pedido(long id, EstadoPedido estado, String importeTotal) {

        Pedido p = new Pedido();
        p.setIdPedido(id);
        p.setEstado(estado);
        p.setImporteTotal(new BigDecimal(importeTotal));
        return p;
    }

    private LineaPedidoPlato lineaPedidoPlato(Plato plato, int cantidad, String precioUnitario) {

        LineaPedidoPlato lp = new LineaPedidoPlato();
        lp.setPlato(plato);
        lp.setCantidad(cantidad);
        lp.setPrecioUnitario(new BigDecimal(precioUnitario));
        return lp;
    }

    private LineaPedidoMenuDiario lineaPedidoMenu(int cantidad, String precioUnitario) {

        LineaPedidoMenuDiario lm = new LineaPedidoMenuDiario();
        lm.setMenuDiario(menuHoy);
        lm.setEntrante(entrante);
        lm.setPrincipal(principal);
        lm.setPostre(postre);
        lm.setBebida(bebida);
        lm.setCantidad(cantidad);
        lm.setPrecioUnitario(new BigDecimal(precioUnitario));
        return lm;
    }

    private void prepararMenu() {

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
    @DisplayName("PU-07-01: resumirPedidoTemporal con un plato devuelve 1 línea")
    void resumirPedidoTemporal_conPlato_devuelveResumen_Linea() {

        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(lineaPlatoDTO), List.of());

        assertEquals(1, resumen.getLineas().size());

        ResumenPedidoDTO.Linea linea = resumen.getLineas().getFirst();
        assertFalse(linea.isMenu());
        assertEquals("Plato 10", linea.getNombre());
        assertNull(linea.getDetalle());
    }

    @Test
    @DisplayName("PU-07-02: resumirPedidoTemporal con un plato devuelve 1 total correcto")
    void resumirPedidoTemporal_conPlato_devuelveResumen_Precio() {

        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(lineaPlatoDTO), List.of());

        assertEquals(0, new BigDecimal("10.00").compareTo(resumen.getTotal()));
    }

    @Test
    @DisplayName("PU-07-03: resumirPedidoTemporal con un menú devuelve detalle con los 4 platos")
    void resumirPedidoTemporal_conMenu_devuelveDetalle_Linea() {

        prepararMenu();

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(), List.of(lineaMenuDTO));

        assertEquals(1, resumen.getLineas().size());

        ResumenPedidoDTO.Linea linea = resumen.getLineas().getFirst();
        assertTrue(linea.isMenu());
        assertEquals("Menú del día", linea.getNombre());
        assertEquals("Plato 10 · Plato 11 · Plato 12 · Plato 13", linea.getDetalle());
    }

    @Test
    @DisplayName("PU-07-04: resumirPedidoTemporal con un menú devuelve detalle con el precio total")
    void resumirPedidoTemporal_conMenu_devuelveDetalle_Precio() {

        prepararMenu();

        ResumenPedidoDTO resumen = pedidoService.resumirPedidoTemporal(
                List.of(), List.of(lineaMenuDTO));

        assertEquals(0, new BigDecimal("15.00").compareTo(resumen.getTotal()));
    }

    @Test
    @DisplayName("PU-07-05: crearPedido válido con un plato guarda con total y estado correctos")
    void crearPedido_unPlato_guardaCorrectamente() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.crearPedido(1L, usuario.getDireccion(),
                List.of(lineaPlatoDTO), List.of());

        assertEquals(0, new BigDecimal("10.00").compareTo(resultado.getImporteTotal()));
        assertEquals(EstadoPedido.RECIBIDO, resultado.getEstado());
        assertEquals("Calle Falsa 123", resultado.getDireccionEntrega());
        assertSame(usuario, resultado.getUsuario());
        assertNotNull(resultado.getFechaHora());

        verify(pedidoRepository).save(any(Pedido.class));
        verify(lineaPedidoPlatoRepository).saveAll(any());
        verify(lineaPedidoMenuDiarioRepository).saveAll(any());
    }

    @Test
    @DisplayName("PU-07-06: crearPedido válido con un menú del día guarda con total 15 €")
    void crearPedido_unMenu_guardaConTotalDelMenu() {

        prepararMenu();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.crearPedido(1L, usuario.getDireccion(),
                List.of(), List.of(lineaMenuDTO));

        assertEquals(0, new BigDecimal("15.00").compareTo(resultado.getImporteTotal()));
        verify(lineaPedidoMenuDiarioRepository).saveAll(any());
    }

    @Test
    @DisplayName("PU-07-07: crearPedido vacío lanza IllegalArgumentException")
    void crearPedido_vacio_lanzaError() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(), List.of(), List.of()));

        assertTrue(ex.getMessage().contains("vacío"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-08: crearPedido sin dirección lanza IllegalArgumentException")
    void crearPedido_sinDireccion_lanzaError() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, "   ", List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("dirección"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-09: crearPedido con usuario inexistente lanza RecursoNoEncontrado")
    void crearPedido_usuarioInexistente_lanzaRecursoNoEncontrado() {

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearPedido(99L, usuario.getDireccion(),
                        List.of(lineaPlatoDTO), List.of()));

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-10: crearPedido con un plato retirado de la carta lanza IllegalArgumentException")
    void crearPedido_platoRetirado_lanzaError() {

        lineaPlatoDTO.setIdPlato(platoRetirado.getIdPlato());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(platoRepository.findById(20L)).thenReturn(Optional.of(platoRetirado));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("ya no está en la carta"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-11: crearPedido con cantidad 0 lanza IllegalArgumentException")
    void crearPedido_cantidadCero_lanzaError() {

        lineaPlatoDTO.setCantidad(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().contains("cantidad"));
    }

    @Test
    @DisplayName("PU-07-12: crearPedido con menú que no es de hoy lanza IllegalArgumentException")
    void crearPedido_menuNoEsDeHoy_lanzaError() {

        lineaMenuDTO.setIdMenuDiario(menuAyer.getIdMenuDiario());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(menuDiarioRepository.findById(2L)).thenReturn(Optional.of(menuAyer));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().contains("hoy"));
    }

    @Test
    @DisplayName("PU-07-13: crearPedido con dirección demasiado larga lanza IllegalArgumentException")
    void crearPedido_direccionDemasiadoLarga_lanzaError() {

        String direccion = "a".repeat(256);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, direccion,
                        List.of(lineaPlatoDTO), List.of()));

        assertTrue(ex.getMessage().toLowerCase().contains("255"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-14-01: crearPedido con menú cuyo entrante no está en las opciones del menú lanza IllegalArgumentException")
    void crearPedido_entranteNoEsOpcionDelMenu_lanzaError() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(menuDiarioRepository.findById(1L)).thenReturn(Optional.of(menuHoy));
        when(platoRepository.findById(13L)).thenReturn(Optional.of(bebida));
        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.ENTRANTE))
                .thenReturn(List.of(opcion(menuHoy, entrante, CategoriaMenu.ENTRANTE)));

        lineaMenuDTO.setIdEntrante(principal.getIdPlato());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().contains("entrante"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-14-02: crearPedido con menú cuyo principal no está en las opciones del menú lanza IllegalArgumentException")
    void crearPedido_principalNoEsOpcionDelMenu_lanzaError() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(menuDiarioRepository.findById(1L)).thenReturn(Optional.of(menuHoy));
        when(platoRepository.findById(13L)).thenReturn(Optional.of(bebida));
        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.ENTRANTE))
                .thenReturn(List.of(opcion(menuHoy, entrante, CategoriaMenu.ENTRANTE)));
        when(menuDiarioOpcionRepository
                .findByMenuDiarioIdMenuDiarioAndCategoria(1L, CategoriaMenu.PRINCIPAL))
                .thenReturn(List.of(opcion(menuHoy, principal, CategoriaMenu.PRINCIPAL)));

        lineaMenuDTO.setIdPrincipal(postre.getIdPlato());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().contains("principal"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-14-03: crearPedido con menú cuyo postre no está en las opciones del menú lanza IllegalArgumentException")
    void crearPedido_postreNoEsOpcionDelMenu_lanzaError() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        prepararMenu();

        lineaMenuDTO.setIdPostre(principal.getIdPlato());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().contains("postre"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-07-15: crearPedido con menú cuya bebida no es de categoría BEBIDA lanza IllegalArgumentException")
    void crearPedido_bebidaNoEsBebida_lanzaError() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(menuDiarioRepository.findById(1L)).thenReturn(Optional.of(menuHoy));
        when(platoRepository.findById(10L)).thenReturn(Optional.of(entrante));

        lineaMenuDTO.setIdBebida(entrante.getIdPlato());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearPedido(1L, usuario.getDireccion(),
                        List.of(), List.of(lineaMenuDTO)));

        assertTrue(ex.getMessage().toLowerCase().contains("bebida"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PU-08-01: obtenerResumenDePedido devuelve las líneas guardadas y el importe")
    void obtenerResumenDePedido_devuelveLineas() {

        Pedido pedido = pedido(5L, EstadoPedido.RECIBIDO, "25.00");
        LineaPedidoPlato lp = lineaPedidoPlato(entrante, 1, "10.00");
        LineaPedidoMenuDiario lm = lineaPedidoMenu(1, "15.00");

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(lineaPedidoPlatoRepository.findByPedidoIdPedido(5L)).thenReturn(List.of(lp));
        when(lineaPedidoMenuDiarioRepository.findByPedidoIdPedido(5L)).thenReturn(List.of(lm));

        ResumenPedidoDTO resumen = pedidoService.obtenerResumenDePedido(5L);

        assertEquals(2, resumen.getLineas().size());
        assertEquals(0, new BigDecimal("25.00").compareTo(resumen.getTotal()));
    }

    @Test
    @DisplayName("PU-08-02: obtenerResumenDePedido con pedido inexistente lanza RecursoNoEncontrado")
    void obtenerResumenDePedido_noExiste_lanzaRecursoNoEncontrado() {

        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.obtenerResumenDePedido(99L));
    }

    @Test
    @DisplayName("PU-08-03: buscarPorId devuelve el pedido con su estado actual")
    void buscarPorId_devuelvePedidoConEstado() {

        Pedido pedido = pedido(1L, EstadoPedido.EN_PREPARACION, "10.00");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.buscarPorId(1L);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
    }

    @Test
    @DisplayName("PU-08-04: buscarPorId lanza RecursoNoEncontrado si no existe")
    void buscarPorId_noExiste_lanzaRecursoNoEncontrado() {

        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("PU-08-05: listarPedidosDeUsuario devuelve cada pedido con su estado")
    void listarPedidosDeUsuario_devuelveCadaPedidoConSuEstado() {

        List<Pedido> pedidos = List.of(
                pedido(1L, EstadoPedido.RECIBIDO,       "10.00"),
                pedido(2L, EstadoPedido.EN_PREPARACION, "10.00"),
                pedido(3L, EstadoPedido.EN_REPARTO,     "10.00"),
                pedido(4L, EstadoPedido.ENTREGADO,      "10.00"),
                pedido(5L, EstadoPedido.CANCELADO,      "10.00")
        );

        when(pedidoRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(1L))
                .thenReturn(pedidos);

        List<Pedido> resultado = pedidoService.listarPedidosDeUsuario(1L);

        assertEquals(5, resultado.size());
        assertEquals(EstadoPedido.RECIBIDO,       resultado.get(0).getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, resultado.get(1).getEstado());
        assertEquals(EstadoPedido.EN_REPARTO,     resultado.get(2).getEstado());
        assertEquals(EstadoPedido.ENTREGADO,      resultado.get(3).getEstado());
        assertEquals(EstadoPedido.CANCELADO,      resultado.get(4).getEstado());
    }
}
