package com.gymadmin.repositorio;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Contrato generico de acceso a entidades dentro de la transaccion del servicio. */
public interface IRepositorio<T> {
    /**
     * Persiste sin abrir ni confirmar otra transaccion.
     *
     * @param pm administrador perteneciente al caso de uso
     * @param entidad objeto a guardar
     * @return objeto persistente del mismo contexto
     */
    T guardar(PersistenceManager pm, T entidad);

    /**
     * Recupera una entidad o informa que no existe.
     *
     * @param pm administrador del caso de uso
     * @param id identidad solicitada
     * @return objeto persistente, aun vinculado a pm
     */
    T porId(PersistenceManager pm, long id);

    /**
     * Materializa una consulta parametrizada y cierra sus recursos.
     *
     * @param pm administrador del caso de uso
     * @param filtro expresion JDOQL interna, nunca texto del usuario
     * @param parametros valores separados de la expresion
     * @param orden orden JDOQL interno, o cadena vacia
     * @return lista materializada de objetos del contexto
     */
    List<T> listar(PersistenceManager pm, String filtro, Map<String, ?> parametros, String orden);
}
