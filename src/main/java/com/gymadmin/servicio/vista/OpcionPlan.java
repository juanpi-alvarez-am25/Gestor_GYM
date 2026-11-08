package com.gymadmin.servicio.vista;

import com.gymadmin.dominio.*;

import java.time.LocalDate;

/** Vista previa del precio y periodo de cada plan disponible. */
public class OpcionPlan {
    private final PlanMembresia plan;
    private final LocalDate inicio;
    private final LocalDate vencimiento;

    /**
     * Construye una respuesta independiente del PersistenceManager.
     *
     * @param plan plan separado del contexto
     * @param inicio primer dia estimado
     * @param vencimiento ultimo dia estimado
     */
    public OpcionPlan(PlanMembresia plan, LocalDate inicio, LocalDate vencimiento) {
        this.plan = plan;
        this.inicio = inicio;
        this.vencimiento = vencimiento;
    }

    /**
     * @return plan separado del contexto
     */
    public PlanMembresia getPlan() {
        return plan;
    }

    /**
     * @return primer dia estimado
     */
    public LocalDate getInicio() {
        return inicio;
    }

    /**
     * @return ultimo dia estimado
     */
    public LocalDate getVencimiento() {
        return vencimiento;
    }
}
