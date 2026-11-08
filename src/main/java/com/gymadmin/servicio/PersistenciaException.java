package com.gymadmin.servicio;

/** Fallo de infraestructura que la web presenta sin revelar datos de conexion. */
public class PersistenciaException extends RuntimeException {
    /**
     * @param causa fallo original conservado para el diagnostico tecnico
     */
    public PersistenciaException(Throwable causa) {
        super(
                "No se pudo completar la operación en la base de datos. Reintente; si persiste,"
                    + " revise la conexión o un posible dato duplicado.",
                causa);
    }
}
