package com.gymadmin.dominio;

/** Medios registrados en comprobantes internos; no procesa cobros externos. */
public enum MetodoPago {
    /** Pago recibido en caja. */
    EFECTIVO,
    /** Transferencia ya verificada por recepcion. */
    TRANSFERENCIA,
    /** Pago recibido por terminal de tarjetas. */
    TARJETA
}
