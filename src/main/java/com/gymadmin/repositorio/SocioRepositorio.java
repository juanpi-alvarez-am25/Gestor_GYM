package com.gymadmin.repositorio;

import com.gymadmin.dominio.Socio;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Busquedas de socios por datos personales, numero y estado. */
@Repository
public class SocioRepositorio extends RepositorioJdo<Socio> {
    /** Configura el tipo Socio. */
    public SocioRepositorio() {
        super(Socio.class);
    }

    /**
     * Busca sin insertar texto externo en la expresion JDOQL.
     *
     * @param pm administrador de la unidad
     * @param texto nombre, apellido, DNI o numero S000001
     * @param estado TODOS, ACTIVOS o INACTIVOS
     * @return socios ordenados por apellido y nombre
     */
    public List<Socio> buscar(PersistenceManager pm, String texto, String estado) {
        String q = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
        long numero = -1;
        if (q.matches("s[0-9]+")) {
            try {
                numero = Long.parseLong(q.substring(1));
            } catch (NumberFormatException ignored) {
                numero = -1;
            }
        }
        String filtro =
                "(nombre.toLowerCase().indexOf(:q) >= 0 || apellido.toLowerCase().indexOf(:q) >= 0"
                    + " || dni.indexOf(:dni) >= 0 || id == :numero)";
        if ("ACTIVOS".equals(estado)) filtro += " && activo == true";
        if ("INACTIVOS".equals(estado)) filtro += " && activo == false";
        return listar(
                pm,
                filtro,
                Map.of("q", q, "dni", q.replaceAll("[.\\s-]", ""), "numero", numero),
                "apellido ascending, nombre ascending");
    }
}
