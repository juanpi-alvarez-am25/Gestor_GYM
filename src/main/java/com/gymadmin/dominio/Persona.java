package com.gymadmin.dominio;

import javax.jdo.annotations.*;

/**
 * Datos comunes persistidos una sola vez en PERSONAS. La identidad y el DNI unico pertenecen a la
 * raiz de la jerarquia (R01).
 */
@PersistenceCapable(table = "PERSONAS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME, column = "CLASE")
public abstract class Persona {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @Column(name = "ID")
    private long id;

    @Column(allowsNull = "false", length = 80)
    private String nombre;

    @Column(allowsNull = "false", length = 80)
    private String apellido;

    @Unique(name = "UQ_PERSONA_DNI")
    @Column(allowsNull = "false", length = 9)
    private String dni;

    @Column(length = 32)
    private String telefono;

    /** Constructor reservado a JDO. */
    protected Persona() {}

    /**
     * Crea datos personales validos antes de persistir.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento con separadores opcionales
     * @param telefono contacto opcional
     */
    protected Persona(String nombre, String apellido, String dni, String telefono) {
        actualizarDatos(nombre, apellido, dni, telefono);
    }

    /**
     * Modifica los datos sin cambiar identidad ni relaciones historicas.
     *
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo documento; el servicio verifica unicidad global
     * @param telefono contacto opcional, de hasta 32 caracteres
     */
    public void actualizarDatos(String nombre, String apellido, String dni, String telefono) {
        this.nombre = Validaciones.texto(nombre, "Nombre", 80);
        this.apellido = Validaciones.texto(apellido, "Apellido", 80);
        this.dni = Validaciones.dni(dni);
        if (telefono != null && telefono.strip().length() > 32) {
            throw new ReglaNegocioException("El teléfono admite hasta 32 caracteres.");
        }
        this.telefono = telefono == null ? "" : telefono.strip();
    }

    /**
     * @return identidad generada por MySQL; cero mientras el objeto es transitorio
     */
    public long getId() {
        return id;
    }

    /**
     * @return nombre de pila
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * @return DNI normalizado
     */
    public String getDni() {
        return dni;
    }

    /**
     * @return contacto, posiblemente vacio
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @return nombre legible para listados y comprobantes
     */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
