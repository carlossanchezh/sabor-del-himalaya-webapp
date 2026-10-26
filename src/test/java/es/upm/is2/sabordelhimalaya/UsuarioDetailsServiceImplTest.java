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

// Pruebas unitarias de US-03 (Inicio de sesión): UsuarioDetailsServiceImpl.loadUserByUsername()
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
        usuarioActivo.setLogin("joselogin");
        usuarioActivo.setContrasena("123456");
        usuarioActivo.setEstadoActivacion(true);
        usuarioActivo.setRol(Rol.CLIENTE);
    }

    @Test
    @DisplayName("CPU-03.1: Usuario activo devuelve un UserDetails con username y password correctos")
    void loadUserByUsername_usuarioActivo_devuelveUserDetailsCorrecto() {

        when(usuarioRepository.findByLogin("joselogin")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("joselogin");

        assertNotNull(userDetails);
        assertEquals("joselogin", userDetails.getUsername());
        assertEquals("123456", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("CPU-03.2: La autoridad devuelta lleva el prefijo ROLE_")
    void loadUserByUsername_autoridadConPrefijoRole() {

        when(usuarioRepository.findByLogin("joselogin")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("joselogin");

        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CLIENTE", authority.getAuthority());
    }

    @Test
    @DisplayName("CPU-03.3: Usuario con cuenta no activada se construye con isEnabled()=false")
    void loadUserByUsername_usuarioInactivo_seConstruyeDeshabilitado() {

        usuarioActivo.setEstadoActivacion(false);
        when(usuarioRepository.findByLogin("joselogin")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("joselogin");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("CPU-03.4: Login inexistente lanza UsernameNotFoundException")
    void loadUserByUsername_usuarioInexistente_lanzaExcepcion() {

        when(usuarioRepository.findByLogin("desconocido")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("desconocido")
        );

        assertTrue(ex.getMessage().contains("desconocido"));
    }

    @Test
    @DisplayName("CPU-03.5: La contraseña cifrada se pasa al UserDetails sin modificar")
    void loadUserByUsername_contrasenaCifradaSeMantieneIgual() {

        when(usuarioRepository.findByLogin("joselogin")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("joselogin");

        assertEquals("123456", userDetails.getPassword());
    }
}
