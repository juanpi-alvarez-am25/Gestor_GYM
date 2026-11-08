package com.gymadmin.controlador.formulario;

import com.gymadmin.dominio.*;

import jakarta.validation.constraints.*;

/** Solicitud de contrato sin campo de precio controlado por el navegador. */
public class ContratoFormulario {
    @NotNull(message = "Seleccione un plan.")
    @Positive
    private Long planId;

    @NotNull(message = "Seleccione el método de pago.")
    private MetodoPago metodo;

    @NotBlank
    @Pattern(
            regexp = "[0-9a-fA-F-]{36}",
            message = "Reabra el formulario para generar una operación válida.")
    private String claveOperacion;

    /** Constructor utilizado por el enlace de formularios MVC. */
    public ContratoFormulario() {}

    /**
     * @return identidad del plan elegido
     */
    public Long getPlanId() {
        return planId;
    }

    /**
     * @param planId identidad del plan elegido
     */
    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    /**
     * @return medio de pago recibido
     */
    public MetodoPago getMetodo() {
        return metodo;
    }

    /**
     * @param metodo medio de pago recibido
     */
    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    /**
     * @return UUID idempotente de la solicitud
     */
    public String getClaveOperacion() {
        return claveOperacion;
    }

    /**
     * @param claveOperacion UUID idempotente de la solicitud
     */
    public void setClaveOperacion(String claveOperacion) {
        this.claveOperacion = claveOperacion;
    }
}
