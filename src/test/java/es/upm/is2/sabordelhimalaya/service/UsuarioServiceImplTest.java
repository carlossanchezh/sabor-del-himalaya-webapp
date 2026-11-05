package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.dto.request.RegistroUsuarioDTO;
import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-01 (Registro)
 - US-02 (Activación)
 - US-03 (Inicio de sesión)
 */

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private RegistroUsuarioDTO dtoValido;
    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;

    @BeforeEach
    void setUp() {

        // DTO válido para registro
        dtoValido = new RegistroUsuarioDTO();
        dtoValido.setNombre("Usuario 1");
        dtoValido.setDireccion("Calle Falsa 123");
        dtoValido.setCorreo("usuario1@test.com");
        dtoValido.setLogin("usuario1");
        dtoValido.setContrasena("123456");

        // Usuario activo: login y correo existentes, cuenta ya activada
        usuarioActivo = new Usuario();
        usuarioActivo.setIdUsuario(1L);
        usuarioActivo.setLogin("usuario2");
        usuarioActivo.setCorreo("usuario2@test.com");
        usuarioActivo.setEstadoActivacion(true);

        // Usuario inactivo: login y correo existentes, cuenta sin activar
        usuarioInactivo = new Usuario();
        usuarioInactivo.setIdUsuario(2L);
        usuarioInactivo.setLogin("usuario3");
        usuarioInactivo.setCorreo("usuario3@test.com");
        usuarioInactivo.setEstadoActivacion(false);
    }

    @Test
    @DisplayName("PU-01-01: Registro válido guarda el usuario con datos correctos y cuenta inactiva")
    void registrar_datosValidos_guardaUsuarioCorrectamente() {

        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(usuarioRepository.existsByLogin(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("contrasenaCifradaMock");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.registrar(dtoValido);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();

        assertEquals(dtoValido.getNombre(), usuarioGuardado.getNombre());
        assertEquals(dtoValido.getDireccion(), usuarioGuardado.getDireccion());
        assertEquals(dtoValido.getCorreo(), usuarioGuardado.getCorreo());
        assertEquals(dtoValido.getLogin(), usuarioGuardado.getLogin());

        assertFalse(usuarioGuardado.getEstadoActivacion());
        assertNotNull(usuarioGuardado.getTokenActivacion());
        assertEquals(Rol.CLIENTE, usuarioGuardado.getRol());

        assertEquals("contrasenaCifradaMock", usuarioGuardado.getContrasena());
        assertNotEquals(dtoValido.getContrasena(), usuarioGuardado.getContrasena());
    }

    @Test
    @DisplayName("PU-01-02: Rechaza el registro si el login ya existe")
    void registrar_loginExistente_lanzaExcepcion() {

        when(usuarioRepository.existsByLogin(dtoValido.getLogin())).thenReturn(true);

        IllegalArgumentException  ex = assertThrows(
                IllegalArgumentException .class,
                () -> usuarioService.registrar(dtoValido)
        );

        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("login") || msg.contains("usuario"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("PU-01-03: Rechaza el registro si el correo ya existe")
    void registrar_correoExistente_lanzaExcepcion() {

        when(usuarioRepository.existsByCorreo(dtoValido.getCorreo())).thenReturn(true);

        IllegalArgumentException  ex = assertThrows(
                IllegalArgumentException .class,
                () -> usuarioService.registrar(dtoValido)
        );

        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("correo"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("PU-02-01: Registrar envía el correo con el mismo token que se guarda")
    void registrar_enviaCorreoConElMismoTokenGuardado() {

        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(usuarioRepository.existsByLogin(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.registrar(dtoValido);

        ArgumentCaptor<Usuario> userCaptor = ArgumentCaptor.forClass(Usuario.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        verify(usuarioRepository).save(userCaptor.capture());
        verify(emailService).enviarCorreoActivacion(userCaptor.capture(), tokenCaptor.capture());

        Usuario guardado = userCaptor.getValue();
        assertEquals(guardado.getTokenActivacion(), tokenCaptor.getValue());
        assertNotNull(guardado.getTokenActivacion());
    }

    @Test
    @DisplayName("PU-02-02: Activar cuenta con token válido actualiza el estado y borra el token")
    void activarCuenta_tokenValido_activaUsuario() {

        usuarioInactivo.setTokenActivacion("token-valido-123");
        when(usuarioRepository.findByTokenActivacion("token-valido-123")).thenReturn(Optional.of(usuarioInactivo));

        boolean resultado = usuarioService.activarCuenta("token-valido-123");

        assertTrue(resultado);

        verify(usuarioRepository).save(usuarioInactivo);
        assertTrue(usuarioInactivo.getEstadoActivacion());
        assertNull(usuarioInactivo.getTokenActivacion());
    }

    @Test
    @DisplayName("PU-02-03: Activar cuenta con token inválido devuelve false y no guarda")
    void activarCuenta_tokenInvalido_devuelveFalse() {

        when(usuarioRepository.findByTokenActivacion("token-falso")).thenReturn(Optional.empty());

        boolean resultado = usuarioService.activarCuenta("token-falso");

        assertFalse(resultado);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("PU-02-04: Un token ya usado no se puede reutilizar")
    void activarCuenta_tokenYaUsado_devuelveFalse() {

        when(usuarioRepository.findByTokenActivacion("token-consumido"))
                .thenReturn(Optional.empty());

        boolean resultado = usuarioService.activarCuenta("token-consumido");

        assertFalse(resultado);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("PU-02-05: Reenviar activación a cuenta ya activa lanza excepción")
    void reenviarActivacion_cuentaYaActiva_lanzaExcepcion() {

        when(usuarioRepository.findByLogin("usuario2"))
                .thenReturn(Optional.of(usuarioActivo));

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> usuarioService.reenviarActivacion("usuario2")
        );

        assertTrue(ex.getMessage().contains("ya está activada"));

        // Verificamos que no se envía ningún correo
        verify(emailService, never()).enviarCorreoActivacion(any(), anyString());
    }

    @Test
    @DisplayName("PU-02-06: Reenviar activación a cuenta inactiva genera nuevo token y envía correo")
    void reenviarActivacion_cuentaInactiva_enviaCorreo() {

        usuarioInactivo.setTokenActivacion("token-viejo");
        when(usuarioRepository.findByLogin("usuario3"))
                .thenReturn(Optional.of(usuarioInactivo));

        usuarioService.reenviarActivacion("usuario3");

        verify(usuarioRepository).save(usuarioInactivo);
        assertNotNull(usuarioInactivo .getTokenActivacion()); // Debe tener un token nuevo
        assertNotEquals("token-viejo", usuarioInactivo.getTokenActivacion());
        verify(emailService).enviarCorreoActivacion(eq(usuarioInactivo), anyString());
    }

    @Test
    @DisplayName("PU-02-07: Reenviar activación por correo a cuenta inactiva genera nuevo token y envía correo")
    void reenviarActivacion_porCorreo_enviaCorreo() {

        usuarioInactivo.setTokenActivacion("token-viejo");
        when(usuarioRepository.findByCorreo("usuario3@test.com"))
                .thenReturn(Optional.of(usuarioInactivo));

        usuarioService.reenviarActivacion("usuario3@test.com");

        verify(usuarioRepository).save(usuarioInactivo);
        assertNotNull(usuarioInactivo .getTokenActivacion()); // Debe tener un token nuevo
        assertNotEquals("token-viejo", usuarioInactivo.getTokenActivacion());
        verify(emailService).enviarCorreoActivacion(eq(usuarioInactivo), anyString());
    }

    @Test
    @DisplayName("PU-02-08: Reenviar activación con correo inexistente lanza excepción")
    void reenviarActivacion_correoInexistente_lanzaRecursoNoEncontrado() {

        when(usuarioRepository.findByCorreo("noexiste@test.com"))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.reenviarActivacion("noexiste@test.com")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).enviarCorreoActivacion(any(), anyString());
    }

    @Test
    @DisplayName("PU-02-09: Reenviar activación con login inexistente lanza excepción")
    void reenviarActivacion_loginInexistente_lanzaRecursoNoEncontrado() {

        when(usuarioRepository.findByLogin("noexiste"))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.reenviarActivacion("noexiste")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("login"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(emailService, never()).enviarCorreoActivacion(any(), anyString());
    }

    @Test
    @DisplayName("PU-03-01: buscarPorLogin devuelve el usuario si existe")
    void buscarPorLogin_existe_devuelveUsuario() {

        when(usuarioRepository.findByLogin("usuario2"))
                .thenReturn(Optional.of(usuarioActivo));

        assertSame(usuarioActivo, usuarioService.buscarPorLogin("usuario2"));
    }

    @Test
    @DisplayName("PU-03-02: buscarPorLogin lanza RecursoNoEncontrado si no existe")
    void buscarPorLogin_noExiste_lanzaRecursoNoEncontrado() {

        when(usuarioRepository.findByLogin("noexiste")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> usuarioService.buscarPorLogin("noexiste"));
    }
}