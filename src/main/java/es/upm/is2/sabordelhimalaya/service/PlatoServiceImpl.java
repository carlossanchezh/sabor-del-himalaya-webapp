package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;

import es.upm.is2.sabordelhimalaya.dto.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.dto.PlatoModificacionDTO;

import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;

    @Override
    public List<Plato> listarCarta() {
        // TODO (US-04): return platoRepository.findByEstaEnCartaTrue();
        throw new UnsupportedOperationException("TODO US-04");
    }

    @Override
    public List<Plato> listarCartaPorCategoria(CategoriaPlato categoria) {
        // TODO (US-04): return platoRepository.findByEstaEnCartaTrueAndCategoria(categoria);
        throw new UnsupportedOperationException("TODO US-04");
    }

    @Override
    public List<Plato> listarPlatosDelChef() {
        // TODO (US-14): return platoRepository.findByEsDelChefTrue();
        throw new UnsupportedOperationException("TODO US-14");
    }

    @Override
    public List<Plato> listarPlatosRetiradosDeLaCarta() {
        // TODO (US-18): return platoRepository.findByEstaEnCartaFalseAndEsDelChefFalse();
        throw new UnsupportedOperationException("TODO US-18");
    }

    @Override
    public Plato crearPlatoFueraDeCarta(PlatoCreacionDTO dto) {
        // TODO (US-14): Crear Plato con estaEnCarta=false, esDelChef=true.
        // Poner precio del palto creado por el chef a 0
        throw new UnsupportedOperationException("TODO US-14");
    }

    @Override
    public Plato anadirPlatoALaCarta(PlatoCreacionDTO dto) {
        // TODO (US-17): Crear Plato con estaEnCarta=true, esDelChef=false.
        throw new UnsupportedOperationException("TODO US-17");
    }

    @Override
    public Plato anadirPlatoExistenteALaCarta(Long idPlato) {
        // TODO (US-17): Buscar plato, poner estaEnCarta=true, guardar.
        throw new UnsupportedOperationException("TODO US-17");
    }

    @Override
    public Plato modificarPlato(PlatoModificacionDTO dto) {
        // TODO (US-16): Buscar plato, actualizar campos y guardar.
        throw new UnsupportedOperationException("TODO US-16");
    }

    @Override
    public void retirarPlatoDeLaCarta(Long id) {
        // TODO (US-18): Buscar plato, poner estaEnCarta=false, guardar.
        throw new UnsupportedOperationException("TODO US-18");
    }

    @Override
    public Plato buscarPorId(Long id) {
        // TODO: return platoRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Plato> listarTodosPlatos() {
        // TODO: return platoRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }
}