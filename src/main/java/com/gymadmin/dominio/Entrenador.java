package com.gymadmin.dominio;

import javax.jdo.annotations.*;

/** Especializacion persistente de Empleado con especialidad y horario obligatorios. */
@PersistenceCapable(table = "ENTRENADORES", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Entrenador extends Empleado {
    @Column(allowsNull = "false", jdbcType = "VARCHAR", length = 24)
    private Especialidad especialidad;

    @Column(allowsNull = "false", length = 120)
    private String horario;

    /** Constructor reservado a JDO. */
    protected Entrenador() {}

    /**
     * Crea un entrenador cuyo tipo siempre es ENTRENADOR.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento
     * @param telefono contacto opcional
     * @param especialidad area de entrenamiento
     * @param horario texto del horario laboral
     */
    public Entrenador(
            String nombre,
            String apellido,
            String dni,
            String telefono,
            Especialidad especialidad,
            String horario) {
        super(nombre, apellido, dni, telefono, TipoEmpleado.ENTRENADOR);
        actualizarEntrenamiento(especialidad, horario);
    }

    /**
     * Actualiza los datos propios sin cambiar la identidad del empleado.
     *
     * @param especialidad area de entrenamiento
     * @param horario horario laboral obligatorio
     */
    public void actualizarEntrenamiento(Especialidad especialidad, String horario) {
        if (especialidad == null)
            throw new ReglaNegocioException("Seleccione la especialidad del entrenador.");
        this.especialidad = especialidad;
        this.horario = Validaciones.texto(horario, "Horario", 120);
    }

    /**
     * @return area de entrenamiento
     */
    public Especialidad getEspecialidad() {
        return especialidad;
    }

    /**
     * @return horario laboral informado
     */
    public String getHorario() {
        return horario;
    }
}
