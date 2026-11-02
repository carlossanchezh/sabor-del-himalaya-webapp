package es.upm.is2.sabordelhimalaya.dto.session;

import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoMenuDiarioDTO;
import es.upm.is2.sabordelhimalaya.dto.request.LineaPedidoPlatoDTO;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class PedidoTemporalDTO implements Serializable {

    // Clave utilizada para guardar y recuperar el pedido de la HttpSession
    private static final String ATRIBUTO_SESION = "pedidoTemporal";

    // Listas internas (líneas de pedido) que componen el pedido temporal de la sesión
    private final List<LineaPedidoPlatoDTO> lineasPlato = new ArrayList<>();
    private final List<LineaPedidoMenuDiarioDTO> lineasMenu = new ArrayList<>();

    // Devuelve el pedido de la sesión (lo crea vacío si todavía no existe)
    public static PedidoTemporalDTO deSesion(HttpSession session) {

        // Recupera el pedido de la HttpSession
        PedidoTemporalDTO pedido = (PedidoTemporalDTO) session.getAttribute(ATRIBUTO_SESION);

        // Si todavía no existe lo crea vacío
        if (pedido == null) {
            pedido = new PedidoTemporalDTO();
            session.setAttribute(ATRIBUTO_SESION, pedido);
        }

        return pedido;
    }

    // Si el plato ya está en el pedido temporal se suma la cantidad
    public void anadirPlato(LineaPedidoPlatoDTO nueva) {

        for (LineaPedidoPlatoDTO linea : lineasPlato) {

            if (linea.getIdPlato().equals(nueva.getIdPlato())) {

                linea.setCantidad(linea.getCantidad() + nueva.getCantidad());
                return;
            }
        }

        lineasPlato.add(nueva);
    }

    // Si el menú del día tiene exactamente la misma selección de platos se suma la cantidad
    public void anadirMenu(LineaPedidoMenuDiarioDTO nueva) {

        for (LineaPedidoMenuDiarioDTO linea : lineasMenu) {

            if (mismaSeleccion(linea, nueva)) {

                linea.setCantidad(linea.getCantidad() + nueva.getCantidad());
                return;
            }
        }

        lineasMenu.add(nueva);
    }

    // Comprueba si el pedido está vacío
    public boolean estaVacio() {

        return lineasPlato.isEmpty() && lineasMenu.isEmpty();
    }

    // Vacía el pedido
    public void vaciar() {

        lineasPlato.clear();
        lineasMenu.clear();
    }

    // Compara dos líneas de menú diario para saber si el cliente ha elegido exactamente los mismos platos
    private boolean mismaSeleccion(LineaPedidoMenuDiarioDTO lineaA, LineaPedidoMenuDiarioDTO lineaB) {

        return Objects.equals(lineaA.getIdMenuDiario(), lineaB.getIdMenuDiario())
                && Objects.equals(lineaA.getIdEntrante(), lineaB.getIdEntrante())
                && Objects.equals(lineaA.getIdPrincipal(), lineaB.getIdPrincipal())
                && Objects.equals(lineaA.getIdPostre(), lineaB.getIdPostre())
                && Objects.equals(lineaA.getIdBebida(), lineaB.getIdBebida());
    }
}
