package com.gymadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** Punto de entrada y paquete base del contenedor Spring. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class GymAdminApplication {
    /**
     * Inicia el servidor web y sus componentes.
     *
     * @param args argumentos de configuracion de Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(GymAdminApplication.class, args);
    }
}
