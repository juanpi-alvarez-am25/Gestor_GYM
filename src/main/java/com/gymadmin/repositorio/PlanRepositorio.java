package com.gymadmin.repositorio;

import com.gymadmin.dominio.PlanMembresia;

import org.springframework.stereotype.Repository;

/** Acceso al catalogo de planes. */
@Repository
public class PlanRepositorio extends RepositorioJdo<PlanMembresia> {
    /** Configura el tipo PlanMembresia. */
    public PlanRepositorio() {
        super(PlanMembresia.class);
    }
}
