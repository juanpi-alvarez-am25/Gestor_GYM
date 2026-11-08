package com.gymadmin.dominio;

import java.math.BigDecimal;

import javax.jdo.annotations.*;

/** Catalogo de condiciones actuales; cada contrato conserva su propia copia (R08). */
@PersistenceCapable(table = "PLANES", detachable = "true")
public class PlanMembresia {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private long id;

    @Unique(name = "UQ_PLAN_NOMBRE")
    @Column(allowsNull = "false", length = 40)
    private String nombre;

    @Column(allowsNull = "false", jdbcType = "DECIMAL", length = 10, scale = 2)
    private BigDecimal precio;

    private int duracionDias;
    private int limiteSemanal;
    private boolean activo = true;

    /** Constructor reservado a JDO. */
    protected PlanMembresia() {}

    /**
     * Define un plan ofertable.
     *
     * @param nombre nombre unico del catalogo
     * @param precio importe positivo
     * @param duracionDias cantidad positiva de dias inclusivos
     * @param limiteSemanal dias distintos por semana; cero significa ilimitado
     */
    public PlanMembresia(String nombre, BigDecimal precio, int duracionDias, int limiteSemanal) {
        this.nombre = Validaciones.texto(nombre, "Nombre del plan", 40);
        actualizarCondiciones(precio, duracionDias, limiteSemanal);
    }

    /**
     * Cambia solo las condiciones para futuras contrataciones.
     *
     * @param precio nuevo importe positivo
     * @param duracionDias nueva duracion, de 1 a 3660 dias
     * @param limiteSemanal limite entre 0 (ilimitado) y 7
     */
    public void actualizarCondiciones(BigDecimal precio, int duracionDias, int limiteSemanal) {
        if (duracionDias < 1 || duracionDias > 3660 || limiteSemanal < 0 || limiteSemanal > 7) {
            throw new ReglaNegocioException("Revise la duración y los días semanales del plan.");
        }
        this.precio = Validaciones.importe(precio);
        this.duracionDias = duracionDias;
        this.limiteSemanal = limiteSemanal;
    }

    /**
     * @return identidad persistente
     */
    public long getId() {
        return id;
    }

    /**
     * @return nombre comercial
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return importe actual del catalogo
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * @return duracion actual en dias
     */
    public int getDuracionDias() {
        return duracionDias;
    }

    /**
     * @return dias distintos por semana; cero equivale a ilimitado
     */
    public int getLimiteSemanal() {
        return limiteSemanal;
    }

    /**
     * @return disponibilidad para nuevas contrataciones
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * @param activo disponibilidad, sin modificar contratos anteriores
     */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }
}
