package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.MenuDiario;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;

import es.upm.is2.sabordelhimalaya.dto.request.MenuDiarioDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MenuDiarioService {

    // US-05: Usuario consultar el menu diario de hoy
    Optional<MenuDiario> consultarMenuDeFecha(LocalDate fecha);

    // US-05: Opciones (entrantes, principales y postres) configuradas en un menú diario
    List<MenuDiarioOpcion> listarOpcionesDeMenu(Long idMenuDiario);

    // US-13: Chef configurar los platos del menu diario
    MenuDiario configurarMenu(MenuDiarioDTO dto);

    MenuDiario buscarPorId(Long id);

    List<MenuDiario> listarTodosMenuDiario();
}
