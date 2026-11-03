package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

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
    @DisplayName("CPU-03.3: Usuario activo devuelve un UserDetails con username y password correctos")
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
    @DisplayName("CPU-03.4: El rol CLIENTE se mapea a ROLE_CLIENTE")
    void loadUserByUsername_rolCliente_seMapeaConPrefijo() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());

        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CLIENTE", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.5: El rol CHEF se mapea a ROLE_CHEF")
    void loadUserByUsername_rolChef_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.CHEF);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());

        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CHEF", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.6: El rol DUENO se mapea a ROLE_DUENO")
    void loadUserByUsername_rolDueno_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.DUENO);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_DUENO", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.7: El rol STAFF se mapea a ROLE_STAFF")
    void loadUserByUsername_rolStaff_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.STAFF);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_STAFF", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.8: Usuario con cuenta no activada se construye con isEnabled()=false")
    void loadUserByUsername_usuarioInactivo_seConstruyeDeshabilitado() {

        usuarioActivo.setEstadoActivacion(false);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("CPU-03.9: Login inexistente lanza UsernameNotFoundException")
    void loadUserByUsername_usuarioInexistente_lanzaUsernameNotFoundException() {

        when(usuarioRepository.findByLogin("noexiste")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("noexiste"));

        assertTrue(ex.getMessage().contains("noexiste"));
    }
}
