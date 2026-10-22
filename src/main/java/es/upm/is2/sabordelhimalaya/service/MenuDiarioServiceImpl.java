package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.MenuDiario;

import es.upm.is2.sabordelhimalaya.dto.MenuDiarioDTO;

import es.upm.is2.sabordelhimalaya.repository.MenuDiarioOpcionRepository;
import es.upm.is2.sabordelhimalaya.repository.MenuDiarioRepository;
import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuDiarioServiceImpl implements MenuDiarioService {

    private final MenuDiarioRepository menuDiarioRepository;
    private final MenuDiarioOpcionRepository menuDiarioOpcionRepository;
    private final PlatoRepository platoRepository;

    @Override
    public Optional<MenuDiario> consultarMenuDeFecha(LocalDate fecha) {
        // TODO (US-05): return menuDiarioRepository.findByFecha(fecha);
        throw new UnsupportedOperationException("TODO US-05");
    }

    @Override
    public MenuDiario configurarMenu(MenuDiarioDTO dto) {
        // TODO (US-13): Validar 3 entrantes, 3 principales y 2 postres.
        // Crear el MenuDiario, guardarlo y crear las opciones en MenuDiarioOpcion.
        throw new UnsupportedOperationException("TODO US-13");
    }

    @Override
    public MenuDiario buscarPorId(Long id) {
        // TODO: return menuDiarioRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<MenuDiario> listarTodosMenuDiario() {
        // TODO: return menuDiarioRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }
}

