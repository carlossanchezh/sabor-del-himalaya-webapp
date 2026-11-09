package es.upm.is2.sabordelhimalaya;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Disabled
@SpringBootTest
class EmailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void enviarCorreoDePrueba() {

        LocalDateTime hora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String horaEnvio = hora.format(formato);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("sabordelhimalaya@gmail.com");
        mensaje.setTo("sabordelhimalaya@gmail.com");
        mensaje.setSubject("Prueba del sistema de correo");
        mensaje.setText("El sistema de correo funciona correctamente.\n" + "Correo enviado a las: " + horaEnvio);

        assertDoesNotThrow(() -> mailSender.send(mensaje));

    }
}
