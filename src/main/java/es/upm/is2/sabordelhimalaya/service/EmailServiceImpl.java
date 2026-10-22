package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void enviarCorreoActivacion(Usuario usuario, String token) {
        // TODO (US-02): Construir enlace con token, crear SimpleMailMessage y enviar.
        throw new UnsupportedOperationException("TODO US-02");
    }
}
