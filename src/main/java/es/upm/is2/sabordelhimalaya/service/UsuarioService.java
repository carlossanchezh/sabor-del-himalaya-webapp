package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

import es.upm.is2.sabordelhimalaya.dto.request.RegistroUsuarioDTO;

import java.util.List;

public interface UsuarioService {

    // US-01: Registrar un nuevo usuario
    Usuario registrar(RegistroUsuarioDTO dto);

    // US-02: Activar la cuenta
    boolean activarCuenta(String token);

    // US-02: Reenviar el correo de activación
    void reenviarActivacion(String login);

    // US-03: Buscar usuario para el login
    Usuario buscarPorLogin(String login);

    Usuario buscarPorId(Long id);
    List<Usuario> listarTodosUsuarios();
}
