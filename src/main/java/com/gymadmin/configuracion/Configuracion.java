package com.gymadmin.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/** Infraestructura compartida: fabrica JDO, reloj y proteccion de formularios. */
@Configuration
public class Configuracion {
    /**
     * Combina la unidad XML con credenciales externas; Spring cierra la fabrica.
     *
     * @param url URL JDBC del esquema MySQL
     * @param usuario cuenta de la aplicacion
     * @param clave contrasena recibida desde el entorno
     * @return fabrica unica de administradores JDO
     */
    @Bean(destroyMethod = "close")
    public PersistenceManagerFactory persistenceManagerFactory(
            @Value("${gym.db.url}") String url,
            @Value("${gym.db.user}") String usuario,
            @Value("${gym.db.password}") String clave) {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("javax.jdo.option.ConnectionDriverName", "com.mysql.cj.jdbc.Driver");
        propiedades.put("javax.jdo.option.ConnectionURL", url);
        propiedades.put("javax.jdo.option.ConnectionUserName", usuario);
        propiedades.put("javax.jdo.option.ConnectionPassword", clave);
        return JDOHelper.getPersistenceManagerFactory(propiedades, "gymadmin");
    }

    /**
     * @return reloj de negocio en la zona horaria del gimnasio (R13)
     */
    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));
    }

    /**
     * Conserva CSRF en los POST de la demostracion local, sin introducir roles.
     *
     * @param http configurador de seguridad web
     * @return cadena de filtros para las solicitudes
     * @throws Exception si no se puede construir la cadena
     */
    @Bean
    public SecurityFilterChain formularios(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(reglas -> reglas.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .build();
    }
}
