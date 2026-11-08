package com.gymadmin.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.jdo.annotations.*;

/** Socio con baja logica e historiales que permanecen en la base (R02 a R04). */
@PersistenceCapable(table = "SOCIOS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Socio extends Persona {
    @Column(allowsNull = "false")
    private LocalDate fechaInscripcion;

    private boolean activo = true;

    @Persistent(mappedBy = "socio", defaultFetchGroup = "false")
    private List<Membresia> membresias = new ArrayList<>();

    @Persistent(mappedBy = "socio", defaultFetchGroup = "false")
    private List<Asistencia> asistencias = new ArrayList<>();

    /** Constructor reservado a JDO. */
    protected Socio() {}

    /**
     * Registra un socio activo con fecha del reloj de negocio.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento del socio
     * @param telefono contacto opcional
     * @param fechaInscripcion fecha local de alta
     */
    public Socio(
            String nombre,
            String apellido,
            String dni,
            String telefono,
            LocalDate fechaInscripcion) {
        super(nombre, apellido, dni, telefono);
        this.fechaInscripcion = Objects.requireNonNull(fechaInscripcion);
    }

    /**
     * @return numero derivado de la identidad, estable y sin reutilizar contadores de filas
     */
    public String getNumeroSocio() {
        return "S" + String.format(java.util.Locale.ROOT, "%06d", getId());
    }

    /**
     * @return fecha original de alta
     */
    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    /**
     * @return si puede realizar nuevas operaciones
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Cambia el estado sin borrar movimientos; el servicio controla visitas abiertas.
     *
     * @param activo nuevo estado administrativo
     */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }

    /**
     * @return vista de contratos; consultar dentro de una unidad JDO con la relacion cargada
     */
    public List<Membresia> getMembresias() {
        return Collections.unmodifiableList(membresias);
    }

    /**
     * @return vista de visitas; consultar dentro de una unidad JDO con la relacion cargada
     */
    public List<Asistencia> getAsistencias() {
        return Collections.unmodifiableList(asistencias);
    }
}
