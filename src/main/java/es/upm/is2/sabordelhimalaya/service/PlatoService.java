package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;

import es.upm.is2.sabordelhimalaya.model.Plato;

import es.upm.is2.sabordelhimalaya.dto.request.PlatoCreacionDTO;
import es.upm.is2.sabordelhimalaya.dto.request.PlatoModificacionDTO;

import java.util.List;

public interface PlatoService {

    // Consultas de carta (usuarios / dueño / chef)

    // US-04, US-12, US-15: Usuario, Dueño y Chef consultar la carta
    List<Plato> listarCarta();

    // US-04, US-12: Usuario, Chef consultar la carta por categorías
    List<Plato> listarCartaPorCategoria(CategoriaPlato categoria);

    // US-14: Platos creados por el chef (para reutilizar en menús diarios)
    List<Plato> listarPlatosDelChef(); // Está en carta -> false && Es del chef -> true

    // US-18: Platos que el dueño retiró de la carta
    List<Plato> listarPlatosRetiradosDeLaCarta(); // Está en carta -> false && Es del chef -> false

    // Creación y Modificación

    // US-14: Chef crear plato fuera de carta para usar en menu diario
    Plato crearPlatoFueraDeCarta(PlatoCreacionDTO dto);

    // US-17: Dueño añadir plato a la carta
    Plato anadirPlatoALaCarta(PlatoCreacionDTO dto);
    Plato anadirPlatoExistenteALaCarta(Long idPlato);

    // US-16: Dueño modificar plato de la carta
    Plato modificarPlato(PlatoModificacionDTO dto);

    // Eliminación

    // US-18: Dueño retirar plato de carta
    void retirarPlatoDeLaCarta(Long id);

    Plato buscarPorId(Long id);
    List<Plato> listarTodosPlatos();



}
