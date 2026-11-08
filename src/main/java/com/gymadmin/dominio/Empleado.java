package com.gymadmin.dominio;

import javax.jdo.annotations.*;

/** Personal de recepcion o limpieza, o base persistente de Entrenador. */
@PersistenceCapable(table = "EMPLEADOS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Empleado extends Persona {
    @Column(allowsNull = "false", jdbcType = "VARCHAR", length = 24)
    private TipoEmpleado tipo;

    private boolean activo = true;

    /** Constructor reservado a JDO. */
    protected Empleado() {}

    /**
     * Crea el personal y valida que la funcion coincida con su clase persistente.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento
     * @param telefono contacto opcional
     * @param tipo funcion del empleado
     */
    public Empleado(
            String nombre, String apellido, String dni, String telefono, TipoEmpleado tipo) {
        super(nombre, apellido, dni, telefono);
        cambiarTipo(tipo);
    }

    /**
     * Permite cambios de funcion que no conviertan la clase del objeto.
     *
     * @param tipo nueva funcion
     * @throws ReglaNegocioException si se intenta convertir un entrenador en empleado o viceversa
     */
    public void cambiarTipo(TipoEmpleado tipo) {
        if (tipo == null || ((this instanceof Entrenador) != (tipo == TipoEmpleado.ENTRENADOR))) {
            throw new ReglaNegocioException(
                    "El tipo debe corresponder a la clase del personal; no se convierte un registro"
                        + " en entrenador.");
        }
        this.tipo = tipo;
    }

    /**
     * @return funcion coherente con la clase persistente
     */
    public TipoEmpleado getTipo() {
        return tipo;
    }

    /**
     * @return estado administrativo
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * @param activo nuevo estado, conservando el registro y su identidad
     */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }
}
