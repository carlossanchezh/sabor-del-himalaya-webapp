package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void enviarCorreoActivacion(Usuario usuario, String token) {

        // Enlace apuntando a Auth controller
        String enlace = "http://localhost:8080/auth/activar?token=" + token;

        // Mensaje del email
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(usuario.getCorreo());
        mensaje.setSubject("Activa tu cuenta en Sabor del Himalaya");
        mensaje.setText("Hola " + usuario.getNombre() + ",\n\n" + "Gracias por registrarte en Sabor del Himalaya.\n" + "Para activar tu cuenta, haz clic en el siguiente enlace:\n\n" + enlace + "\n\n" + "Si no has solicitado esto, ignora este correo.");

        // Envío del email
        mailSender.send(mensaje);

    }
}
