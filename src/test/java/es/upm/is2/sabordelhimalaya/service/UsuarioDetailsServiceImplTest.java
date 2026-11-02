package es.upm.is2.sabordelhimalaya;

import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;
import es.upm.is2.sabordelhimalaya.service.UsuarioDetailsServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-03 (Inicio de sesión)
 */

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsServiceImpl usuarioDetailsService;

    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = new Usuario();
        usuarioActivo.setIdUsuario(1L);
        usuarioActivo.setLogin("usuario1");
        usuarioActivo.setContrasena("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        usuarioActivo.setEstadoActivacion(true);
        usuarioActivo.setRol(Rol.CLIENTE);
    }

    @Test
    @DisplayName("CPU-03.1: Usuario activo devuelve un UserDetails con username y password correctos")
    void loadUserByUsername_usuarioActivo_devuelveUserDetailsCorrecto() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertNotNull(userDetails);
        assertEquals("usuario1", userDetails.getUsername());
        assertEquals("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("CPU-03.2: La autoridad devuelta lleva el prefijo ROLE_")
    void loadUserByUsername_autoridadConPrefijoRole() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());

        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CLIENTE", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.3: Usuario con cuenta no activada se construye con isEnabled()=false")
    void loadUserByUsername_usuarioInactivo_seConstruyeDeshabilitado() {

        usuarioActivo.setEstadoActivacion(false);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("CPU-03.4: La contraseña cifrada se pasa al UserDetails sin modificar")
    void loadUserByUsername_contrasenaCifradaSeMantieneIgual() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", userDetails.getPassword());
    }

    @Test
    @DisplayName("CPU-03.5: El username del UserDetails es el login, no el nombre")
    void loadUserByUsername_usernameEsElLoginNoElNombre() {

        when(usuarioRepository.findByLogin("usuario1"))
                .thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals("usuario1", userDetails.getUsername());
        assertNotEquals("Usuario 1", userDetails.getUsername());
    }
}
