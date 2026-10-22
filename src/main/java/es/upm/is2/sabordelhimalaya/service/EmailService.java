package es.upm.is2.sabordelhimalaya.service;

import es.upm.is2.sabordelhimalaya.model.Usuario;

public interface EmailService {

    // US-02: Activar cuenta de usuario
    void enviarCorreoActivacion(Usuario usuario, String token);

}