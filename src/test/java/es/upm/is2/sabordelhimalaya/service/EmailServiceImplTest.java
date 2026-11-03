package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 Pruebas unitarias de:
 - US-02 (Activación)
 */

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("CPU-02.8: Enviar correo construye el mensaje correctamente")
    void enviarCorreoActivacion_construyeMensajeCorrectamente() {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario 1");
        usuario.setCorreo("usuario1@test.com");
        String token = "token-secreto-123";

        emailService.enviarCorreoActivacion(usuario, token);

        // Capturamos el mensaje que se intenta enviar
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage mensajeEnviado = captor.getValue();

        // Verificamos que los datos del mensaje son correctos
        assertArrayEquals(new String[]{"usuario1@test.com"}, mensajeEnviado.getTo());
        assertEquals("Activa tu cuenta en Sabor del Himalaya", mensajeEnviado.getSubject());

        // Verificamos que el cuerpo incluye el nombre, el enlace y el token
        String cuerpo = mensajeEnviado.getText();
        assertNotNull(cuerpo);
        assertTrue(cuerpo.contains("Usuario 1"));
        assertTrue(cuerpo.contains("http://localhost:8080/auth/activar?token=" + token));
    }
}
