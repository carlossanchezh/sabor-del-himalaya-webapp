package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;

import es.upm.is2.sabordelhimalaya.enums.Rol;

import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public Usuario registrar(RegistroUsuarioDTO dto) {

        // comprobar que el correo no está registrado
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new OperacionNoPermitidaException("El correo ya está registrado.");
        }

        // comprobar que el login no está registrado
        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new OperacionNoPermitidaException("El nombre de usuario (login) ya está registrado. Prueba con otro.");
        }

        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setDireccion(dto.getDireccion());
        usuario.setCorreo(dto.getCorreo());
        usuario.setLogin(dto.getLogin());

        // Cifrar la contraseña
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        // Cuenta inactiva y rol de cliente por defecto
        usuario.setEstadoActivacion(false);
        usuario.setRol(Rol.CLIENTE);

        // Generar el token de activación
        usuario.setTokenActivacion(UUID.randomUUID().toString());

        // Guardar en Base de Datos
        return usuarioRepository.save(usuario);
    }

    @Override
    public boolean activarCuenta(String token) {
        // TODO (US-02): Buscar usuario por token.
        // Si existe, poner estadoActivacion=true, borrar token y guardar.
        throw new UnsupportedOperationException("TODO US-02");
    }

    @Override
    public void reenviarActivacion(String login) {
        // TODO (US-02): Buscar usuario, regenerar token, guardar y enviar correo.
        throw new UnsupportedOperationException("TODO US-02");
    }

    @Override
    public Usuario buscarPorLogin(String login) {
        // TODO (US-03): return usuarioRepository.findByLogin(login).orElse(null);
        throw new UnsupportedOperationException("TODO US-03");
    }

    @Override
    public Usuario buscarPorId(Long id) {
        // TODO: return usuarioRepository.findById(id).orElseThrow(...);
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Usuario> listarTodosUsuarios() {
        // TODO: return usuarioRepository.findAll();
        throw new UnsupportedOperationException("TODO");
    }
}
