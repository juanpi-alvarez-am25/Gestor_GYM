package com.gymadmin.servicio;

import org.springframework.stereotype.Component;

import java.util.function.Function;

import javax.jdo.*;

/**
 * Un administrador y una transaccion por caso de uso (R10 y R18). Las escrituras usan lecturas
 * bloqueantes en MySQL y READ COMMITTED. El socio se lee primero para serializar operaciones que
 * comparten su identidad.
 */
@Component
public class UnidadTrabajo {
    private final PersistenceManagerFactory fabrica;

    /**
     * @param fabrica fabrica unica administrada por Spring
     */
    public UnidadTrabajo(PersistenceManagerFactory fabrica) {
        this.fabrica = fabrica;
    }

    /**
     * Ejecuta una consulta y cierra todo el contexto antes de retornar.
     *
     * @param operacion consulta que prepara DTO u objetos detached
     * @return resultado independiente del administrador cerrado
     */
    public <T> T leer(Function<PersistenceManager, T> operacion) {
        return operar(operacion, false);
    }

    /**
     * Confirma todas las escrituras o revierte la unidad ante cualquier excepcion.
     *
     * @param operacion caso de uso con repositorios que comparten pm
     * @return resultado de la operacion confirmada
     */
    public <T> T escribir(Function<PersistenceManager, T> operacion) {
        return operar(operacion, true);
    }

    private <T> T operar(Function<PersistenceManager, T> operacion, boolean bloquear) {
        try (PersistenceManager pm = fabrica.getPersistenceManager()) {
            pm.getFetchPlan().setMaxFetchDepth(3);
            Transaction tx = pm.currentTransaction();
            tx.setSerializeRead(bloquear);
            try {
                tx.begin();
                T resultado = operacion.apply(pm);
                tx.commit();
                return resultado;
            } finally {
                if (tx.isActive()) tx.rollback();
            }
        } catch (JDOException ex) {
            throw new PersistenciaException(ex);
        }
    }
}
