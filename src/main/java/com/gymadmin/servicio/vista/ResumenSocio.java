package com.gymadmin.servicio.vista;

import com.gymadmin.dominio.*;

/** Datos de recepcion preparados antes de cerrar JDO. */
public class ResumenSocio {
    private final Socio socio;
    private final Membresia actual;
    private final Membresia ultimo;
    private final boolean visitaAbierta;
    private final int diasConsumidos;
    private final String motivoRechazo;

    /**
     * Construye una respuesta independiente del PersistenceManager.
     *
     * @param socio socio separado del contexto
     * @param actual contrato vigente o null
     * @param ultimo ultimo periodo o null
     * @param visitaAbierta existencia de una visita abierta
     * @param diasConsumidos fechas distintas consumidas esta semana
     * @param motivoRechazo motivo de rechazo o texto vacio
     */
    public ResumenSocio(
            Socio socio,
            Membresia actual,
            Membresia ultimo,
            boolean visitaAbierta,
            int diasConsumidos,
            String motivoRechazo) {
        this.socio = socio;
        this.actual = actual;
        this.ultimo = ultimo;
        this.visitaAbierta = visitaAbierta;
        this.diasConsumidos = diasConsumidos;
        this.motivoRechazo = motivoRechazo;
    }

    /**
     * @return socio separado del contexto
     */
    public Socio getSocio() {
        return socio;
    }

    /**
     * @return contrato vigente o null
     */
    public Membresia getActual() {
        return actual;
    }

    /**
     * @return ultimo periodo o null
     */
    public Membresia getUltimo() {
        return ultimo;
    }

    /**
     * @return existencia de una visita abierta
     */
    public boolean isVisitaAbierta() {
        return visitaAbierta;
    }

    /**
     * @return fechas distintas consumidas esta semana
     */
    public int getDiasConsumidos() {
        return diasConsumidos;
    }

    /**
     * @return motivo de rechazo o texto vacio
     */
    public String getMotivoRechazo() {
        return motivoRechazo;
    }
}
