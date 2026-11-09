package es.upm.is2.sabordelhimalaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ResumenPedidoDTO {

    // Líneas del pedido
    private final List<Linea> lineas;

    // Importe total del pedido
    private final BigDecimal total;

    // Crea un resumen vacío
    public static ResumenPedidoDTO vacio() {

        return new ResumenPedidoDTO(List.of(), BigDecimal.ZERO.setScale(2));
    }

    // Comprueba si el resumen no tiene líneas (el pedido está vacío)
    public boolean estaVacio() {

        return lineas.isEmpty();
    }

    // Clase interna que representa una línea del pedido
    @Getter
    @AllArgsConstructor
    public static class Linea {

        // true = es un menú diario, false = es un plato de la carta.
        private final boolean menu;

        // Posición de la línea en su lista original del carrito
        private final int indice;

        // Nombre del producto de la línea
        private final String nombre;

        // Opciones elegidas en el menú diario
        private final String detalle;

        // Cantidad de unidades de este producto
        private final int cantidad;

        // Precio de una sola unidad
        private final BigDecimal precioUnitario;

        // Precio total de esta línea
        private final BigDecimal subtotal;
    }
}
