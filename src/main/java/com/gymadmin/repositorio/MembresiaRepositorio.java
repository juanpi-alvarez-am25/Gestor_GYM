package com.gymadmin.repositorio;

import com.gymadmin.dominio.Membresia;
import com.gymadmin.dominio.Socio;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/** Consultas de periodos, ordenados para calcular renovaciones sin solapamientos. */
@Repository
public class MembresiaRepositorio extends RepositorioJdo<Membresia> {
    /** Configura el tipo Membresia. */
    public MembresiaRepositorio() {
        super(Membresia.class);
    }

    /**
     * Obtiene los contratos de un titular con el mas reciente primero.
     *
     * @param pm administrador actual
     * @param socio titular gestionado en pm
     * @return historial por vencimiento descendente
     */
    public List<Membresia> historial(PersistenceManager pm, Socio socio) {
        return listar(pm, "socio == :socio", Map.of("socio", socio), "vencimiento descending");
    }

    /**
     * Recupera solo un periodo para listados y calculo de renovaciones.
     *
     * @param pm administrador actual
     * @param socio titular
     * @return ultimo contrato o null, sin materializar todo el historial
     */
    @SuppressWarnings("unchecked")
    public Membresia ultimo(PersistenceManager pm, Socio socio) {
        Query<Membresia> consulta = pm.newQuery(Membresia.class);
        try {
            consulta.setFilter("socio == :socio");
            consulta.setOrdering("vencimiento descending");
            consulta.setRange(0, 1);
            List<Membresia> lista =
                    (List<Membresia>) consulta.executeWithMap(Map.of("socio", socio));
            return lista.isEmpty() ? null : lista.get(0);
        } finally {
            consulta.closeAll();
        }
    }

    /**
     * Consulta solo los contratos aplicables al dia indicado.
     *
     * @param pm administrador actual
     * @param socio titular
     * @param hoy fecha local
     * @return contrato vigente o null
     */
    public Membresia actual(PersistenceManager pm, Socio socio, LocalDate hoy) {
        List<Membresia> lista =
                listar(
                        pm,
                        "socio == :socio && inicio <= :hoy && vencimiento >= :hoy",
                        Map.of("socio", socio, "hoy", hoy),
                        "inicio descending");
        return lista.isEmpty() ? null : lista.get(0);
    }
}
