package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO (US-03): Buscar usuario por login, validar si está activo,
        // y retornar un objeto User de Spring Security con su Rol.
        throw new UnsupportedOperationException("TODO US-03");
    }
}
