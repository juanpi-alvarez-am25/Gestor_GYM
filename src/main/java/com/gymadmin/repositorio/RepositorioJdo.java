package com.gymadmin.repositorio;

import com.gymadmin.dominio.ReglaNegocioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.*;

/** Implementacion generica que comparte el PersistenceManager recibido por parametro. */
public class RepositorioJdo<T> implements IRepositorio<T> {
    private final Class<T> clase;

    /**
     * @param clase clase persistente consultada por este repositorio
     */
    public RepositorioJdo(Class<T> clase) {
        this.clase = clase;
    }

    /**
     * Persiste en la unidad actual, sin commit propio.
     *
     * @param pm administrador de la unidad
     * @param entidad objeto validado por el servicio
     * @return objeto gestionado por JDO
     */
    @Override
    public T guardar(PersistenceManager pm, T entidad) {
        return pm.makePersistent(entidad);
    }

    /**
     * Busca por identidad generada.
     *
     * @param pm administrador de la unidad
     * @param id identidad positiva
     * @return entidad gestionada
     * @throws ReglaNegocioException si no existe
     */
    @Override
    public T porId(PersistenceManager pm, long id) {
        if (id <= 0) throw new ReglaNegocioException("La identidad solicitada no es válida.");
        try {
            return pm.getObjectById(clase, id);
        } catch (JDOObjectNotFoundException ex) {
            throw new ReglaNegocioException("No se encontró el registro solicitado.");
        }
    }

    /**
     * Ejecuta JDOQL con parametros y libera el resultado antes de devolverlo.
     *
     * @param pm administrador del caso de uso
     * @param filtro expresion del repositorio
     * @param parametros valores del filtro
     * @param orden orden del repositorio
     * @return copia materializada; sus entidades siguen asociadas a pm
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> listar(
            PersistenceManager pm, String filtro, Map<String, ?> parametros, String orden) {
        Query<T> consulta = pm.newQuery(clase);
        try {
            if (filtro != null && !filtro.isBlank()) consulta.setFilter(filtro);
            if (orden != null && !orden.isBlank()) consulta.setOrdering(orden);
            return new ArrayList<>((List<T>) consulta.executeWithMap(parametros));
        } finally {
            consulta.closeAll();
        }
    }
}
