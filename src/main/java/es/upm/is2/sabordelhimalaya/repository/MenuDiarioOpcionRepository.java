package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.enums.CategoriaMenu;
import es.upm.is2.sabordelhimalaya.model.MenuDiarioOpcion;

import java.util.List;

@Repository
public interface MenuDiarioOpcionRepository extends JpaRepository<MenuDiarioOpcion, Long> {

    // todas las opciones de un determinado menu diario
    List<MenuDiarioOpcion> findByMenuDiarioIdMenuDiario(Long idMenuDiario);

    // todas las opciones de un determinado menu diario de una categoria
    List<MenuDiarioOpcion> findByMenuDiarioIdMenuDiarioAndCategoria(
            Long idMenuDiario, CategoriaMenu categoria);
}
