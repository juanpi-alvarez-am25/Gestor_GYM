package com.gymadmin.controlador.formulario;

import com.gymadmin.dominio.*;

import jakarta.validation.constraints.*;

/** Datos de alta o edicion de personal. */
public class PersonalFormulario extends PersonaFormulario {
    @NotNull(message = "Seleccione la función.")
    private TipoEmpleado tipo;

    private Especialidad especialidad;

    @Size(max = 120)
    private String horario;

    /** Constructor utilizado por el enlace de formularios MVC. */
    public PersonalFormulario() {}

    /**
     * @param empleado empleado o entrenador separado del contexto
     */
    public PersonalFormulario(Empleado empleado) {
        super(empleado);
        tipo = empleado.getTipo();
        if (empleado instanceof Entrenador entrenador) {
            especialidad = entrenador.getEspecialidad();
            horario = entrenador.getHorario();
        }
    }

    /**
     * @return funcion del empleado
     */
    public TipoEmpleado getTipo() {
        return tipo;
    }

    /**
     * @param tipo funcion del empleado
     */
    public void setTipo(TipoEmpleado tipo) {
        this.tipo = tipo;
    }

    /**
     * @return especialidad del entrenador
     */
    public Especialidad getEspecialidad() {
        return especialidad;
    }

    /**
     * @param especialidad especialidad del entrenador
     */
    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    /**
     * @return horario del entrenador
     */
    public String getHorario() {
        return horario;
    }

    /**
     * @param horario horario del entrenador
     */
    public void setHorario(String horario) {
        this.horario = horario;
    }
}
