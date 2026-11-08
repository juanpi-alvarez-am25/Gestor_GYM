package com.gymadmin.repositorio;

import com.gymadmin.dominio.Pago;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Pagos y consulta de solicitudes idempotentes. */
@Repository
public class PagoRepositorio extends RepositorioJdo<Pago> {
    /** Configura el tipo Pago. */
    public PagoRepositorio() {
        super(Pago.class);
    }

    /**
     * Localiza el resultado ya confirmado de una solicitud.
     *
     * @param pm administrador de la unidad
     * @param clave UUID normalizado
     * @return pago existente o null
     */
    public Pago porOperacion(PersistenceManager pm, String clave) {
        List<Pago> lista = listar(pm, "claveOperacion == :clave", Map.of("clave", clave), "");
        return lista.isEmpty() ? null : lista.get(0);
    }
}
