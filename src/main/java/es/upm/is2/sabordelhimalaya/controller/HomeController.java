package es.upm.is2.sabordelhimalaya.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Redirige la raíz del proyecto a la carta principal según el rol del usuario autenticado
    @GetMapping("/")
    public String index(Authentication authentication) {
        // Si el usuario no está logueado, Spring Security interceptará esta ruta y lo mandará al login antes de que llegue aquí.
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // Si ya está logueado, le redirigimos a la carta. Redirección inteligente según el rol definido en Spring Security
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            return "redirect:/carta";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CHEF"))) {
            return "redirect:/chef/carta";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DUENO"))) {
            return "redirect:/dueno/panel";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFF"))) {
            return "redirect:/staff/pedidos";
        }

        // Por defecto si hay cualquier discrepancia
        return "redirect:/auth/login";
    }
}