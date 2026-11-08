package com.gymadmin.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.jdo.annotations.*;

/** Visita que conserva la membresia utilizada al ingresar (R14 a R18). */
@PersistenceCapable(table = "ASISTENCIAS", detachable = "true")
public class Asistencia {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private long id;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "SOCIO_ID", allowsNull = "false")
    private Socio socio;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "MEMBRESIA_ID", allowsNull = "false")
    private Membresia membresia;

    @Column(allowsNull = "false")
    private LocalDateTime ingreso;

    private LocalDateTime salida;

    // MySQL admite varios NULL en UNIQUE: solo las visitas abiertas ocupan el socio.
    @Unique(name = "UQ_ASISTENCIA_ABIERTA")
    @Persistent(defaultFetchGroup = "false")
    @Column(name = "SOCIO_ABIERTO_ID", allowsNull = "true")
    private Socio socioAbierto;

    /** Constructor reservado a JDO. */
    protected Asistencia() {}

    /**
     * Abre una visita una vez autorizada por el servicio.
     *
     * @param socio titular de la visita
     * @param membresia contrato del mismo socio y valido al ingresar
     * @param ingreso fecha y hora del ingreso
     */
    public Asistencia(Socio socio, Membresia membresia, LocalDateTime ingreso) {
        this.socio = Objects.requireNonNull(socio);
        this.membresia = Objects.requireNonNull(membresia);
        this.ingreso = Objects.requireNonNull(ingreso);
        if (membresia.getSocio() != socio || !membresia.aplicaEn(ingreso.toLocalDate())) {
            throw new ReglaNegocioException(
                    "La membresía no corresponde al socio y la fecha de ingreso.");
        }
        this.socioAbierto = socio;
    }

    /**
     * Cierra la visita sin volver a comprobar la vigencia del contrato (R17).
     *
     * @param fechaHora instante estrictamente posterior al ingreso
     * @throws ReglaNegocioException si ya estaba cerrada o la hora no es posterior
     */
    public void registrarSalida(LocalDateTime fechaHora) {
        if (salida != null || fechaHora == null || !fechaHora.isAfter(ingreso)) {
            throw new ReglaNegocioException(
                    "La salida debe cerrar una visita abierta y ser posterior al ingreso.");
        }
        this.salida = fechaHora;
        this.socioAbierto = null;
    }

    /**
     * @return identidad de la visita
     */
    public long getId() {
        return id;
    }

    /**
     * @return titular
     */
    public Socio getSocio() {
        return socio;
    }

    /**
     * @return contrato que autorizo el ingreso
     */
    public Membresia getMembresia() {
        return membresia;
    }

    /**
     * @return instante local de ingreso
     */
    public LocalDateTime getIngreso() {
        return ingreso;
    }

    /**
     * @return instante de salida, o null mientras permanece abierta
     */
    public LocalDateTime getSalida() {
        return salida;
    }

    /**
     * @return si todavia no se registro salida
     */
    public boolean isAbierta() {
        return salida == null;
    }
}
