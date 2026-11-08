package com.gymadmin.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.jdo.annotations.*;

/** Periodo contratado inmutable: referencia el plan y conserva sus condiciones. */
@PersistenceCapable(table = "MEMBRESIAS", detachable = "true")
public class Membresia {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private long id;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "SOCIO_ID", allowsNull = "false")
    private Socio socio;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "PLAN_ID", allowsNull = "false")
    private PlanMembresia plan;

    @Column(allowsNull = "false")
    private LocalDate inicio;

    @Column(allowsNull = "false")
    private LocalDate vencimiento;

    @Column(allowsNull = "false", jdbcType = "DECIMAL", length = 10, scale = 2)
    private BigDecimal precioAcordado;

    private int duracionDias;
    private int limiteSemanal;

    /** Constructor reservado a JDO. */
    protected Membresia() {}

    /**
     * Copia condiciones y calcula el vencimiento inclusivo (R06 y R08).
     *
     * @param socio titular del periodo
     * @param plan oferta consultada por el servicio
     * @param inicio primer dia contratado
     */
    public Membresia(Socio socio, PlanMembresia plan, LocalDate inicio) {
        this.socio = Objects.requireNonNull(socio);
        this.plan = Objects.requireNonNull(plan);
        this.inicio = Objects.requireNonNull(inicio);
        this.precioAcordado = plan.getPrecio();
        this.duracionDias = plan.getDuracionDias();
        this.limiteSemanal = plan.getLimiteSemanal();
        this.vencimiento = inicio.plusDays(duracionDias - 1L);
    }

    /**
     * Evalua vigencia sin confundirla con el estado administrativo del socio.
     *
     * @param fecha dia a consultar
     * @return si la fecha esta incluida en ambos extremos del periodo
     */
    public boolean aplicaEn(LocalDate fecha) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(vencimiento);
    }

    /**
     * @return identidad del periodo
     */
    public long getId() {
        return id;
    }

    /**
     * @return titular del contrato
     */
    public Socio getSocio() {
        return socio;
    }

    /**
     * @return referencia al plan de origen
     */
    public PlanMembresia getPlan() {
        return plan;
    }

    /**
     * @return primer dia incluido
     */
    public LocalDate getInicio() {
        return inicio;
    }

    /**
     * @return ultimo dia incluido
     */
    public LocalDate getVencimiento() {
        return vencimiento;
    }

    /**
     * @return precio historico
     */
    public BigDecimal getPrecioAcordado() {
        return precioAcordado;
    }

    /**
     * @return duracion historica
     */
    public int getDuracionDias() {
        return duracionDias;
    }

    /**
     * @return limite historico, con cero para ilimitado
     */
    public int getLimiteSemanal() {
        return limiteSemanal;
    }
}
