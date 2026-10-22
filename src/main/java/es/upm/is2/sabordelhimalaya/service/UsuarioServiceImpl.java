package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;

import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public Usuario registrar(RegistroUsuarioDTO dto) {
        // TODO (US-01): Validar que el login no exista.
        // Cifrar contraseña con passwordEncoder. (config/SecurityConfig)
        // Generar token UUID, guardar usuario con estadoActivacion=false.
        // Enviar correo.
        throw new UnsupportedOperationException("TODO US-01");
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
