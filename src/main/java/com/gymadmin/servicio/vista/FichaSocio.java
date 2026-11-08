package com.gymadmin.servicio.vista;

import com.gymadmin.dominio.*;

import java.util.List;

/** Ficha e historiales preparados para la vista sin consultas tardias. */
public class FichaSocio {
    private final ResumenSocio resumen;
    private final List<Membresia> membresias;
    private final List<Pago> pagos;
    private final List<Asistencia> asistencias;

    /**
     * Construye una respuesta independiente del PersistenceManager.
     *
     * @param resumen estado actual del socio
     * @param membresias historial de contratos
     * @param pagos historial de pagos
     * @param asistencias historial de visitas
     */
    public FichaSocio(
            ResumenSocio resumen,
            List<Membresia> membresias,
            List<Pago> pagos,
            List<Asistencia> asistencias) {
        this.resumen = resumen;
        this.membresias = List.copyOf(membresias);
        this.pagos = List.copyOf(pagos);
        this.asistencias = List.copyOf(asistencias);
    }

    /**
     * @return estado actual del socio
     */
    public ResumenSocio getResumen() {
        return resumen;
    }

    /**
     * @return historial de contratos
     */
    public List<Membresia> getMembresias() {
        return membresias;
    }

    /**
     * @return historial de pagos
     */
    public List<Pago> getPagos() {
        return pagos;
    }

    /**
     * @return historial de visitas
     */
    public List<Asistencia> getAsistencias() {
        return asistencias;
    }
}
