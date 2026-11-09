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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private PasswordEncoder passwordEncoder;
    private DaoAuthenticationProvider provider;

    private Usuario usuarioActivo;     // login usuario1, cuenta activada
    private Usuario usuarioInactivo;   // login usuario2, cuenta sin activar

    private static final String RAW_PASSWORD = "password123";

    @BeforeEach
    void setUp() {

        passwordEncoder = new BCryptPasswordEncoder();
        provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        usuarioActivo = new Usuario();
        usuarioActivo.setIdUsuario(1L);
        usuarioActivo.setLogin("usuario1");
        usuarioActivo.setContrasena(passwordEncoder.encode(RAW_PASSWORD));
        usuarioActivo.setEstadoActivacion(true);
        usuarioActivo.setRol(Rol.CLIENTE);

        usuarioInactivo = new Usuario();
        usuarioInactivo.setIdUsuario(2L);
        usuarioInactivo.setLogin("usuario2");
        usuarioInactivo.setContrasena(passwordEncoder.encode(RAW_PASSWORD));
        usuarioInactivo.setEstadoActivacion(false);
        usuarioInactivo.setRol(Rol.CLIENTE);
    }

    @Test
    @DisplayName("PU-03-03: Usuario activo devuelve un UserDetails con username y password correctos")
    void loadUserByUsername_usuarioActivo_devuelveUserDetailsCorrecto() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertNotNull(userDetails);
        assertEquals("usuario1", userDetails.getUsername());
        assertEquals(usuarioActivo.getContrasena(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("PU-03-04: Usuario registrado con credenciales correctas inicia sesión")
    void authenticate_credencialesCorrectas_iniciaSesion() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        Authentication token = new UsernamePasswordAuthenticationToken("usuario1", RAW_PASSWORD);
        Authentication resultado = provider.authenticate(token);


        assertTrue(resultado.isAuthenticated());
        assertEquals("usuario1", resultado.getName());
        assertTrue(resultado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    @DisplayName("PU-03-05: Login inexistente lanza UsernameNotFoundException")
    void loadUserByUsername_usuarioInexistente_lanzaUsernameNotFoundException() {

        when(usuarioRepository.findByLogin("noexiste")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("noexiste"));

        assertTrue(ex.getMessage().contains("noexiste"));
    }

    @Test
    @DisplayName("PU-03-06: Usuario registrado con contraseña incorrecta no inicia sesión (CA3.3)")
    void authenticate_contrasenaIncorrecta_noIniciaSesion() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        Authentication token = new UsernamePasswordAuthenticationToken("usuario1", "contrasenaIncorrecta");

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(token));
    }

    @Test
    @DisplayName("PU-03-07: Usuario con cuenta no activada no puede iniciar sesión")
    void authenticate_cuentaNoActivada_noIniciaSesion() {

        when(usuarioRepository.findByLogin("usuario2")).thenReturn(Optional.of(usuarioInactivo));

        Authentication token = new UsernamePasswordAuthenticationToken("usuario2", RAW_PASSWORD);

        assertThrows(DisabledException.class, () -> provider.authenticate(token));
    }

    @Test
    @DisplayName("PU-03-08: El rol CLIENTE se mapea a ROLE_CLIENTE")
    void loadUserByUsername_rolCliente_seMapeaConPrefijo() {

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());

        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CLIENTE", authority.getAuthority());
    }

    @Test
    @DisplayName("PU-03-09: El rol CHEF se mapea a ROLE_CHEF")
    void loadUserByUsername_rolChef_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.CHEF);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());

        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CHEF", authority.getAuthority());
    }

    @Test
    @DisplayName("PU-03-10: El rol DUENO se mapea a ROLE_DUENO")
    void loadUserByUsername_rolDueno_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.DUENO);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_DUENO", authority.getAuthority());
    }

    @Test
    @DisplayName("PU-03-11: El rol STAFF se mapea a ROLE_STAFF")
    void loadUserByUsername_rolStaff_seMapeaConPrefijo() {

        usuarioActivo.setRol(Rol.STAFF);
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("usuario1");

        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_STAFF", authority.getAuthority());
    }
}
