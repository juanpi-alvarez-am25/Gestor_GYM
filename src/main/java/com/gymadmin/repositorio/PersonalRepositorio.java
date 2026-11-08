package com.gymadmin.repositorio;

import com.gymadmin.dominio.Empleado;

import org.springframework.stereotype.Repository;

/** Repositorio polimorfico de empleados y entrenadores. */
@Repository
public class PersonalRepositorio extends RepositorioJdo<Empleado> {
    /** Incluye las instancias de Entrenador en las consultas. */
    public PersonalRepositorio() {
        super(Empleado.class);
    }
}
