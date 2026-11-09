package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;

import es.upm.is2.sabordelhimalaya.dto.request.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.dto.request.PlatoModificacionDTO;

import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;

import es.upm.is2.sabordelhimalaya.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;

    @Override
    public List<Plato> listarCarta() {
        // Productos de la carta
        return platoRepository.findByEstaEnCartaTrue();
    }

    @Override
    public List<Plato> listarCartaPorCategoria(CategoriaPlato categoria) {
        // Productos de la carta de una categoría concreta
        return platoRepository.findByEstaEnCartaTrueAndCategoria(categoria);
    }

    @Override
    public List<Plato> listarPlatosDelChef() {
        // TODO (US-14): return platoRepository.findByEsDelChefTrue();
        throw new UnsupportedOperationException("TODO US-14");
    }

    @Override
    public List<Plato> listarPlatosRetiradosDeLaCarta() {
        // Obtener los platos retirados de la carta
        return platoRepository.findByEstaEnCartaFalseAndEsDelChefFalse();
    }

    @Override
    public Plato crearPlatoFueraDeCarta(PlatoCreacionDTO dto) {
        // TODO (US-14): Crear Plato con estaEnCarta=false, esDelChef=true.
        // Poner precio del palto creado por el chef a 0
        throw new UnsupportedOperationException("TODO US-14");
    }

    @Override
    @Transactional
    public Plato anadirPlatoALaCarta(PlatoCreacionDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Debes indicar los datos del plato.");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del plato es obligatorio.");
        }
        if (dto.getDescripcion() == null || dto.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del plato es obligatoria.");
        }
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(new BigDecimal("0.01")) < 0) {
            throw new IllegalArgumentException("El precio del plato debe ser de al menos 0.01.");
        }
        if(dto.getPrecio().compareTo(new BigDecimal("999999.99")) > 0){
            throw new IllegalArgumentException("El precio del plato no puede superar 999999.99.");
        }
        if (dto.getCategoria() == null) {
            throw new IllegalArgumentException("Debes seleccionar la categoria del plato.");
        }

        // Validar que no existe un plato con ese nombre ya
        boolean existePlato = platoRepository.existsByNombreIgnoreCaseAndEsDelChefFalse(dto.getNombre().trim());

        if (existePlato) {
            throw new IllegalArgumentException("Ya existe un producto con el nombre '" + dto.getNombre().trim() + "'. Si estaba retirado, usa la opción 'Añadir Producto' de la lista de retirados.");
        }

        // Crear nuevo plato
        Plato plato = new Plato();

        // Añadir nombre, descripción, precio y categoría
        plato.setNombre(dto.getNombre().trim());
        plato.setDescripcion(dto.getDescripcion().trim());
        plato.setPrecio(dto.getPrecio().setScale(2, RoundingMode.HALF_UP));
        plato.setCategoria(dto.getCategoria());

        // Plato creado por el chef
        plato.setEstaEnCarta(true);
        plato.setEsDelChef(false);

        return platoRepository.save(plato);
    }

    @Override
    @Transactional
    public Plato anadirPlatoExistenteALaCarta(Long idPlato) {

        // Obtiene el palto
        Plato plato = buscarPorId(idPlato);

        // Control de seguridad evitar añadir a carta un plato del chef o que ya está en carta
        if(plato.getEsDelChef()){
            throw new OperacionNoPermitidaException("Los platos creados por el chef no se pueden añadir a la carta.");
        }
        if(plato.getEstaEnCarta()){
            throw new IllegalArgumentException("El plato '" + plato.getNombre() + "' ya está en la carta.");
        }

        // Poner el plato visible en carta
        plato.setEstaEnCarta(true);

        return platoRepository.save(plato);
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
        //Obtener un plato concreto
        return platoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el plato con id " + id + "."));
    }

    @Override
    public List<Plato> listarTodosPlatos() {
        // Obtener todos los platos
        return platoRepository.findAll();
    }
}