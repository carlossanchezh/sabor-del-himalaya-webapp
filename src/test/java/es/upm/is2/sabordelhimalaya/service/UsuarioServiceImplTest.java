package es.upm.is2.sabordelhimalaya;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;
import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
import es.upm.is2.sabordelhimalaya.exception.RecursoNoEncontradoException;
import es.upm.is2.sabordelhimalaya.model.Usuario;
import es.upm.is2.sabordelhimalaya.repository.UsuarioRepository;

import es.upm.is2.sabordelhimalaya.service.EmailService;
import es.upm.is2.sabordelhimalaya.service.UsuarioServiceImpl;
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

    @BeforeEach
    void setUp() {

        dtoValido = new RegistroUsuarioDTO();
        dtoValido.setNombre("Usuario 1");
        dtoValido.setDireccion("Calle Falsa 123");
        dtoValido.setCorreo("usuario1@test.com");
        dtoValido.setLogin("usuario1");
        dtoValido.setContrasena("123456");
    }

    @Test
    @DisplayName("CPU-01.1: Registro válido guarda el usuario con datos correctos y cuenta inactiva")
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
    @DisplayName("CPU-01.2: Rechaza el registro si el login ya existe")
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
    @DisplayName("CPU-01.3: Rechaza el registro si el correo ya existe")
    void registrar_correoExistente_lanzaExcepcion() {

        when(usuarioRepository.existsByCorreo(dtoValido.getCorreo())).thenReturn(true);

        IllegalArgumentException  ex = assertThrows(
                IllegalArgumentException .class,
                () -> usuarioService.registrar(dtoValido)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CPU-02.1: Activar cuenta con token válido actualiza el estado y borra el token")
    void activarCuenta_tokenValido_activaUsuario() {

        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setIdUsuario(1L);
        usuarioInactivo.setEstadoActivacion(false);
        usuarioInactivo.setTokenActivacion("token-valido-123");

        when(usuarioRepository.findByTokenActivacion("token-valido-123")).thenReturn(Optional.of(usuarioInactivo));

        boolean resultado = usuarioService.activarCuenta("token-valido-123");

        assertTrue(resultado);

        // Capturamos el usuario para verificar que se ha modificado
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();

        assertTrue(usuarioGuardado.getEstadoActivacion());
        assertNull(usuarioGuardado.getTokenActivacion());
    }

    @Test
    @DisplayName("CPU-02.2: Activar cuenta con token inválido devuelve false y no guarda")
    void activarCuenta_tokenInvalido_devuelveFalse() {

        when(usuarioRepository.findByTokenActivacion("token-falso")).thenReturn(Optional.empty());

        boolean resultado = usuarioService.activarCuenta("token-falso");

        assertFalse(resultado);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CPU-02.3: Un token ya usado no se puede reutilizar")
    void activarCuenta_tokenYaUsado_devuelveFalse() {

        when(usuarioRepository.findByTokenActivacion("token-consumido"))
                .thenReturn(Optional.empty());

        boolean resultado = usuarioService.activarCuenta("token-consumido");

        assertFalse(resultado);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CPU-02.4: Reenviar activación a cuenta ya activa lanza excepción")
    void reenviarActivacion_cuentaYaActiva_lanzaExcepcion() {

        Usuario usuarioActivo = new Usuario();
        usuarioActivo.setLogin("usuario1");
        usuarioActivo.setEstadoActivacion(true);

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuarioActivo));

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> usuarioService.reenviarActivacion("usuario1")
        );

        assertTrue(ex.getMessage().contains("ya está activada"));

        // Verificamos que no se envía ningún correo
        verify(emailService, never()).enviarCorreoActivacion(any(), anyString());
    }

    @Test
    @DisplayName("CPU-02.5: Reenviar activación a cuenta inactiva genera nuevo token y envía correo")
    void reenviarActivacion_cuentaInactiva_enviaCorreo() {

        Usuario inactivo  = new Usuario();
        inactivo .setLogin("usuario1");
        inactivo .setEstadoActivacion(false);

        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(inactivo ));

        usuarioService.reenviarActivacion("usuario1");

        verify(usuarioRepository).save(inactivo );
        assertNotNull(inactivo .getTokenActivacion()); // Debe tener un token nuevo
        verify(emailService).enviarCorreoActivacion(eq(inactivo ), anyString());
    }

    @Test
    @DisplayName("CPU-02.6: Reenviar activación con correo inexistente lanza excepción")
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
    @DisplayName("CPU-02.7: Reenviar activación con login inexistente lanza excepción")
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
}