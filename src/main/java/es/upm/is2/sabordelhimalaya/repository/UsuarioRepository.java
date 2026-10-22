package es.upm.is2.sabordelhimalaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // buscar usuario por login
    Optional<Usuario> findByLogin(String login);

    // comprobar si un login ya existe
    boolean existsByLogin(String login);

    // buscar usuario por token de activación
    Optional<Usuario> findByTokenActivacion(String tokenActivacion);

}
