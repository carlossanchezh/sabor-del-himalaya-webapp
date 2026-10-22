package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.model.MenuDiario;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MenuDiarioRepository extends JpaRepository<MenuDiario, Long> {

    // Consultar menú diario de una determinada fecha
    Optional<MenuDiario> findByFecha(LocalDate fecha);

    // Comprobar si ya existe un menu para una fecha determinada
    boolean existsByFecha(LocalDate fecha);
}