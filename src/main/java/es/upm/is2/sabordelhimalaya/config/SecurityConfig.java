package es.upm.is2.sabordelhimalaya.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/css/**", "/js/**", "/img/**", "/error/**").permitAll() // rutas públicas
                        // Reglas por rol
                        .requestMatchers("/chef/**").hasRole("CHEF")
                        .requestMatchers("/staff/**").hasRole("STAFF")
                        .requestMatchers("/dueno/**").hasRole("DUENO")
                        .requestMatchers("/pedidos/**").hasRole("CLIENTE")
                        .requestMatchers("/carta/**").hasRole("CLIENTE")
                        .anyRequest().authenticated() // rutas que requieren login
                )
                .formLogin(form -> form
                        .loginPage("/auth/login") // pantalla de login
                        .successHandler(customSuccessHandler()) // Redirección por rol
                        .failureHandler((request, response, exception) -> {

                            // error cuenta está desactivada
                            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                // Obtenemos el login que el usuario intentó usar
                                String loginFallido = request.getParameter("login");
                                // Le redirigimos a la nueva pantalla de cuenta inactiva
                                response.sendRedirect("/auth/cuenta-inactiva?login=" + loginFallido);
                            } else {
                                // error contraseña mal, le mandamos el error por pantalla
                                response.sendRedirect("/auth/login?error");
                            }
                        })
                        .usernameParameter("login")
                        .passwordParameter("contrasena")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") //pantalla logout
                        .logoutSuccessUrl("/auth/login?logout") // a la pantalla de login despues de logout
                        .permitAll()
                );
        return http.build();
    }

    // Redirige a cada rol a su panel correspondiente tras el login
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            switch (role) {
                case "ROLE_CHEF" -> response.sendRedirect("/chef/carta");
                case "ROLE_STAFF" -> response.sendRedirect("/staff/pedidos");
                case "ROLE_DUENO" -> response.sendRedirect("/dueno/panel");
                case "ROLE_CLIENTE" -> response.sendRedirect("/carta");
                default -> {
                    // Si el login no tiene ningún rol invalida y lanza error 403
                    request.getSession().invalidate();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Rol no reconocido: " + role);
                }
            }
        };
    }
}