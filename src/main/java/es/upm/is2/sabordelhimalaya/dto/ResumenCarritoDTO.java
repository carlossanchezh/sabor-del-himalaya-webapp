package es.upm.is2.sabordelhimalaya.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

// US-06 (CA6.5) y US-07 (CA7.1, CA7.2): productos del carrito con sus precios y el importe total
@Getter
@AllArgsConstructor
public class ResumenCarritoDTO {

    private final List<Linea> lineas;
    private final BigDecimal total;

    public static ResumenCarritoDTO vacio() {
        return new ResumenCarritoDTO(List.of(), BigDecimal.ZERO.setScale(2));
    }

    public boolean estaVacio() {
        return lineas.isEmpty();
    }

    @Getter
    @AllArgsConstructor
    public static class Linea {
        private final boolean menu;           // true = menú diario, false = plato de la carta
        private final int indice;             // posición de la línea en su lista del carrito
        private final String nombre;
        private final String detalle;         // platos elegidos del menú ("Entrante · Principal · Postre · Bebida")
        private final int cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;
    }
}
