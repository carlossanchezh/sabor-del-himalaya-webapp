package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.enums.CategoriaPlato;
import es.upm.is2.sabordelhimalaya.model.Plato;

import java.util.List;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {

    // Lista de platos visibles en la carta
    List<Plato> findByEstaEnCartaTrue();

    // Lista de platos visibles en la carta por categoria
    List<Plato> findByEstaEnCartaTrueAndCategoria(CategoriaPlato categoria);

    // Lista de platos fuera de carta
    List<Plato> findByEstaEnCartaFalseAndEsDelChefFalse();

    // Lista de platos creación del chef
    List<Plato> findByEsDelChefTrue();
}

