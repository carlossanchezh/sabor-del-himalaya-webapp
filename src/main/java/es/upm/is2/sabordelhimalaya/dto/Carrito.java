package es.upm.is2.sabordelhimalaya.dto;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// US-06: Carrito temporal del cliente guardado en la HttpSession hasta que confirma el pedido (US-07)
@Getter
public class Carrito {

    private static final String ATRIBUTO_SESION = "carrito";

    private final List<LineaPedidoPlatoDTO> lineasPlato = new ArrayList<>();
    private final List<LineaPedidoMenuDiarioDTO> lineasMenu = new ArrayList<>();

    // Devuelve el carrito de la sesión (lo crea vacío si todavía no existe)
    public static Carrito deSesion(HttpSession session) {
        Carrito carrito = (Carrito) session.getAttribute(ATRIBUTO_SESION);
        if (carrito == null) {
            carrito = new Carrito();
            session.setAttribute(ATRIBUTO_SESION, carrito);
        }
        return carrito;
    }

    // CA6.3 / CA6.4: si el plato ya está en el carrito se suma la cantidad
    public void anadirPlato(LineaPedidoPlatoDTO nueva) {
        for (LineaPedidoPlatoDTO linea : lineasPlato) {
            if (linea.getIdPlato().equals(nueva.getIdPlato())) {
                linea.setCantidad(linea.getCantidad() + nueva.getCantidad());
                return;
            }
        }
        lineasPlato.add(nueva);
    }

    // CA6.1 / CA6.2: un menú con la misma selección de platos se agrupa en la misma línea
    public void anadirMenu(LineaPedidoMenuDiarioDTO nueva) {
        for (LineaPedidoMenuDiarioDTO linea : lineasMenu) {
            if (mismaSeleccion(linea, nueva)) {
                linea.setCantidad(linea.getCantidad() + nueva.getCantidad());
                return;
            }
        }
        lineasMenu.add(nueva);
    }

    public boolean estaVacio() {
        return lineasPlato.isEmpty() && lineasMenu.isEmpty();
    }

    public void vaciar() {
        lineasPlato.clear();
        lineasMenu.clear();
    }

    private boolean mismaSeleccion(LineaPedidoMenuDiarioDTO a, LineaPedidoMenuDiarioDTO b) {
        return Objects.equals(a.getIdMenuDiario(), b.getIdMenuDiario())
                && Objects.equals(a.getIdEntrante(), b.getIdEntrante())
                && Objects.equals(a.getIdPrincipal(), b.getIdPrincipal())
                && Objects.equals(a.getIdPostre(), b.getIdPostre())
                && Objects.equals(a.getIdBebida(), b.getIdBebida());
    }
}
