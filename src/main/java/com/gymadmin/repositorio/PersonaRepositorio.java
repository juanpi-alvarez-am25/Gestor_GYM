package com.gymadmin.repositorio;

import com.gymadmin.dominio.Persona;
import com.gymadmin.dominio.ReglaNegocioException;

import org.springframework.stereotype.Repository;

import java.util.Map;

import javax.jdo.PersistenceManager;

/** Consulta polimorfica de la raiz para comprobar el DNI de cualquier persona. */
@Repository
public class PersonaRepositorio extends RepositorioJdo<Persona> {
    /** Configura el tipo raiz, incluyendo sus subclases. */
    public PersonaRepositorio() {
        super(Persona.class);
    }

    /**
     * Complementa el indice UNIQUE con un mensaje de validacion previo.
     *
     * @param pm administrador de la unidad
     * @param dni documento ya normalizado
     * @param idActual identidad excluida durante una edicion, o cero para alta
     */
    public void exigirDniDisponible(PersistenceManager pm, String dni, long idActual) {
        if (!listar(pm, "dni == :dni && id != :id", Map.of("dni", dni, "id", idActual), "")
                .isEmpty()) {
            throw new ReglaNegocioException(
                    "El DNI ya pertenece a otra persona, incluso si está inactiva.");
        }
    }
}
