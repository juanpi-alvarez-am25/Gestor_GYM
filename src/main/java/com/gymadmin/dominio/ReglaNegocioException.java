package com.gymadmin.dominio;

/** Rechazo esperado de una operacion que debe poder explicarse al usuario. */
public class ReglaNegocioException extends RuntimeException {
    /**
     * @param mensaje motivo concreto del rechazo, sin detalles de infraestructura
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
