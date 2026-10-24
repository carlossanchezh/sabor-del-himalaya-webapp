package es.upm.is2.sabordelhimalaya;

import es.upm.is2.sabordelhimalaya.dto.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;
import es.upm.is2.sabordelhimalaya.service.PlatoServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas unitarias de US-17 (Dueño añade un plato nuevo a la carta): PlatoServiceImpl.anadirPlatoALaCarta()
@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private PlatoServiceImpl platoService;

    private PlatoCreacionDTO dto(String nombre, String descripcion, String precio, CategoriaPlato categoria) {
        PlatoCreacionDTO dto = new PlatoCreacionDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecio(precio != null ? new BigDecimal(precio) : null);
        dto.setCategoria(categoria);
        return dto;
    }

    private PlatoCreacionDTO dtoValido() {
        return dto("Momos de pollo", "Empanadillas al vapor rellenas de pollo", "8.50", CategoriaPlato.ENTRANTE);
    }

    @Test
    @DisplayName("CPU-17.1: Plato válido se guarda en la carta y no es del chef")
    void anadirPlatoALaCarta_valido_guardaPlatoEnCarta() {
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoService.anadirPlatoALaCarta(dtoValido());

        ArgumentCaptor<Plato> captor = ArgumentCaptor.forClass(Plato.class);
        verify(platoRepository).save(captor.capture());
        Plato guardado = captor.getValue();

        assertSame(guardado, resultado);
        assertEquals("Momos de pollo", guardado.getNombre());
        assertEquals("Empanadillas al vapor rellenas de pollo", guardado.getDescripcion());
        assertEquals(0, new BigDecimal("8.50").compareTo(guardado.getPrecio()));
        assertEquals(CategoriaPlato.ENTRANTE, guardado.getCategoria());
        assertTrue(guardado.getEstaEnCarta());
        assertFalse(guardado.getEsDelChef());
    }

    @Test
    @DisplayName("CPU-17.2: Quita los espacios del nombre y la descripción y deja el precio con 2 decimales")
    void anadirPlatoALaCarta_limpiaDatosYRedondeaPrecio() {
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        Plato resultado = platoService.anadirPlatoALaCarta(
                dto("  Dal bhat  ", "  Lentejas con arroz  ", "12.345", CategoriaPlato.PRINCIPAL));

        assertEquals("Dal bhat", resultado.getNombre());
        assertEquals("Lentejas con arroz", resultado.getDescripcion());
        assertEquals(new BigDecimal("12.35"), resultado.getPrecio());
    }

    @Test
    @DisplayName("CPU-17.3: Rechaza el plato si el precio es 0")
    void anadirPlatoALaCarta_precioCero_lanzaError() {
        PlatoCreacionDTO dto = dto("Lassi", "Bebida de yogur", "0", CategoriaPlato.BEBIDA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("precio"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.4: Rechaza el plato si el precio es negativo")
    void anadirPlatoALaCarta_precioNegativo_lanzaError() {
        PlatoCreacionDTO dto = dto("Lassi", "Bebida de yogur", "-3.00", CategoriaPlato.BEBIDA);

        assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
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
        PlatoCreacionDTO dto = dto("Kheer", "", "5.00", CategoriaPlato.POSTRE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("descripcion"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.7: Rechaza el plato si no tiene categoría")
    void anadirPlatoALaCarta_sinCategoria_lanzaError() {
        PlatoCreacionDTO dto = dto("Kheer", "Arroz con leche", "5.00", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(dto));
        assertTrue(ex.getMessage().contains("categoria"));
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CPU-17.8: Rechaza la petición si no llegan datos")
    void anadirPlatoALaCarta_dtoNulo_lanzaError() {
        assertThrows(IllegalArgumentException.class, () -> platoService.anadirPlatoALaCarta(null));
        verify(platoRepository, never()).save(any());
    }
}
