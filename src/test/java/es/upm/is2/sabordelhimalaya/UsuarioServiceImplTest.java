package es.upm.is2.sabordelhimalaya;

import es.upm.is2.sabordelhimalaya.dto.RegistroUsuarioDTO;
import es.upm.is2.sabordelhimalaya.enums.Rol;
import es.upm.is2.sabordelhimalaya.exception.OperacionNoPermitidaException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Pruebas unitarias de US-01 (Registro): UsuarioServiceImpl.registrar()
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
        dtoValido.setNombre("Jose Prueba");
        dtoValido.setDireccion("Calle Falsa 123");
        dtoValido.setCorreo("jose@test.com");
        dtoValido.setLogin("joselogin");
        dtoValido.setContrasena("123456");
    }

    @Test
    @DisplayName("CPU-01.1: Registro válido guarda el usuario con datos correctos y cuenta inactiva")
    void registrar_datosValidos_guardaUsuarioCorrectamente() {

        when(usuarioRepository.existsByLogin(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("contrasenaCifradaMock");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.registrar(dtoValido);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();

        assertEquals(dtoValido.getNombre(), usuarioGuardado.getNombre());
        assertEquals(dtoValido.getDireccion(), usuarioGuardado.getDireccion());
        assertEquals(dtoValido.getCorreo(), usuarioGuardado.getCorreo());
        assertEquals(dtoValido.getLogin(), usuarioGuardado.getLogin());
        assertFalse(usuarioGuardado.getEstadoActivacion());
        assertEquals(Rol.CLIENTE, usuarioGuardado.getRol());
        assertEquals("contrasenaCifradaMock", usuarioGuardado.getContrasena());
        assertNotEquals(dtoValido.getContrasena(), usuarioGuardado.getContrasena());
        assertNotNull(usuarioGuardado.getTokenActivacion());
    }

    @Test
    @DisplayName("CPU-01.2: Rechaza el registro si el login ya existe")
    void registrar_loginExistente_lanzaExcepcion() {

        when(usuarioRepository.existsByLogin(dtoValido.getLogin())).thenReturn(true);

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> usuarioService.registrar(dtoValido)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuario") || ex.getMessage().toLowerCase().contains("login"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CPU-01.3: Rechaza el registro si el correo ya existe")
    void registrar_correoExistente_lanzaExcepcion() {

        when(usuarioRepository.existsByCorreo(dtoValido.getCorreo())).thenReturn(true);

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> usuarioService.registrar(dtoValido)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}