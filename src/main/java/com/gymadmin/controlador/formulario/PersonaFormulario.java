package com.gymadmin.controlador.formulario;

import com.gymadmin.dominio.*;

import jakarta.validation.constraints.*;

/** Datos personales recibidos por MVC. */
public class PersonaFormulario {
    @NotBlank(message = "Ingrese el nombre.")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "Ingrese el apellido.")
    @Size(max = 80)
    private String apellido;

    @NotBlank(message = "Ingrese el DNI.")
    @Size(max = 18)
    private String dni;

    @Size(max = 32)
    private String telefono;

    /** Constructor utilizado por el enlace de formularios MVC. */
    public PersonaFormulario() {}

    /**
     * @param persona datos persistentes separados del contexto para editar
     */
    public PersonaFormulario(Persona persona) {
        nombre = persona.getNombre();
        apellido = persona.getApellido();
        dni = persona.getDni();
        telefono = persona.getTelefono();
    }

    /**
     * @return nombre de pila
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre nombre de pila
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * @param apellido apellido
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * @return DNI con separadores opcionales
     */
    public String getDni() {
        return dni;
    }

    /**
     * @param dni DNI con separadores opcionales
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * @return contacto opcional
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono contacto opcional
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
