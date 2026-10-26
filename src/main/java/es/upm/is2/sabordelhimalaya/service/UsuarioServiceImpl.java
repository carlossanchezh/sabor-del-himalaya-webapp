package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
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
        String token = UUID.randomUUID().toString();
        usuario.setTokenActivacion(token);

        // Guardar en Base de Datos
        Usuario usuarioSave = usuarioRepository.save(usuario);

        // Enviamos el correo de activación
        emailService.enviarCorreoActivacion(usuarioSave, token);

        return usuarioSave;
    }

    @Override
    public boolean activarCuenta(String token) {

        // Buscar al propietario del token
        Usuario usuario = usuarioRepository.findByTokenActivacion(token).orElse(null);

        if (usuario != null) {

            // Al acceder al enlace válido, la cuenta queda activada
            usuario.setEstadoActivacion(true);
            usuario.setTokenActivacion(null); // Se borra el token para que no se pueda reutilizar
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }

    @Override
    public void reenviarActivacion(String logincorreo) {

        Usuario usuario;

        // Buscamos al usuario por su login o correo (si no existe excepción)
        if (logincorreo.contains("@")) {
            usuario = usuarioRepository.findByCorreo(logincorreo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe usuario con ese correo"));
        } else {
            usuario = usuarioRepository.findByLogin(logincorreo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe usuario con ese login"));
        }

        // Si el usuario ya ha activado su cuenta excepción
        if (usuario.getEstadoActivacion()) {
            throw new OperacionNoPermitidaException("La cuenta ya está activada. Puedes iniciar sesión.");
        }
        
        // Generamos un nuevo token
        String nuevoToken = java.util.UUID.randomUUID().toString();
        usuario.setTokenActivacion(nuevoToken);
        usuarioRepository.save(usuario);

        // Volvemos a enviar el correo con el nuevo token
        emailService.enviarCorreoActivacion(usuario, nuevoToken);
    }

    @Override
    public Usuario buscarPorLogin(String login) {
        // TODO: return usuarioRepository.findByLogin(login).orElse(null);
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
