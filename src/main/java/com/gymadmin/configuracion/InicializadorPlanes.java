package com.gymadmin.configuracion;

import com.gymadmin.dominio.PlanMembresia;
import com.gymadmin.repositorio.PlanRepositorio;
import com.gymadmin.servicio.UnidadTrabajo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/** Crea una sola vez el catalogo inicial con importes ficticios de demostracion. */
@Component
public class InicializadorPlanes implements ApplicationRunner {
    private final UnidadTrabajo unidad;
    private final PlanRepositorio planes;

    /**
     * Inyecta la misma infraestructura que utilizan los casos de uso.
     *
     * @param unidad transaccion de inicializacion
     * @param planes catalogo persistente
     */
    public InicializadorPlanes(UnidadTrabajo unidad, PlanRepositorio planes) {
        this.unidad = unidad;
        this.planes = planes;
    }

    /**
     * Agrega planes faltantes sin restablecer precios en cada reinicio.
     *
     * @param args argumentos de arranque recibidos por Spring Boot
     */
    @Override
    public void run(ApplicationArguments args) {
        unidad.escribir(
                pm -> {
                    String[] nombres = {"Bronce", "Plata", "Oro"};
                    int[] limites = {2, 3, 0};
                    String[] precios = {"18000.00", "22000.00", "28000.00"};
                    for (int i = 0; i < nombres.length; i++) {
                        if (planes.listar(pm, "nombre == :nombre", Map.of("nombre", nombres[i]), "")
                                .isEmpty()) {
                            planes.guardar(
                                    pm,
                                    new PlanMembresia(
                                            nombres[i],
                                            new BigDecimal(precios[i]),
                                            30,
                                            limites[i]));
                        }
                    }
                    return null;
                });
    }
}
