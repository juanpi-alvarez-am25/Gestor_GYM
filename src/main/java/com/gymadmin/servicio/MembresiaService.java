package com.gymadmin.servicio;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;
import com.gymadmin.servicio.vista.OpcionPlan;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Contratacion, pago e idempotencia como una sola unidad de negocio. */
@Service
public class MembresiaService {
    private final UnidadTrabajo unidad;
    private final SocioRepositorio socios;
    private final PlanRepositorio planes;
    private final MembresiaRepositorio membresias;
    private final PagoRepositorio pagos;
    private final Clock reloj;

    /**
     * Inyecta repositorios que recibiran el mismo PersistenceManager.
     *
     * @param unidad limite transaccional
     * @param socios titular y bloqueo de su fila
     * @param planes condiciones actuales del servidor
     * @param membresias contratos
     * @param pagos cobros y claves de operacion
     * @param reloj reloj de negocio
     */
    public MembresiaService(
            UnidadTrabajo unidad,
            SocioRepositorio socios,
            PlanRepositorio planes,
            MembresiaRepositorio membresias,
            PagoRepositorio pagos,
            Clock reloj) {
        this.unidad = unidad;
        this.socios = socios;
        this.planes = planes;
        this.membresias = membresias;
        this.pagos = pagos;
        this.reloj = reloj;
    }

    /**
     * Cobra una sola vez una solicitud y guarda periodo y pago atomicamente. Lee primero el socio
     * con bloqueo; despues reconsulta la clave, incluso en reenvios concurrentes.
     *
     * @param socioId identidad del titular
     * @param planId oferta a consultar en el servidor
     * @param metodo medio registrado
     * @param clave UUID de la solicitud
     * @return identidad del pago nuevo o del resultado ya confirmado
     */
    public long contratar(long socioId, long planId, MetodoPago metodo, String clave) {
        String token;
        try {
            token = UUID.fromString(Objects.requireNonNull(clave)).toString();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ReglaNegocioException(
                    "La clave de operación no es válida. Abra nuevamente el formulario.");
        }
        return unidad.escribir(
                pm -> {
                    Socio socio = socios.porId(pm, socioId);
                    pm.retrieve(socio);
                    Pago anterior = pagos.porOperacion(pm, token);
                    if (anterior != null) {
                        if (anterior.getMembresia().getSocio().getId() != socioId
                                || anterior.getMembresia().getPlan().getId() != planId
                                || anterior.getMetodo() != metodo) {
                            throw new ReglaNegocioException(
                                    "La clave ya fue utilizada con otros datos; no se registró otro"
                                        + " cobro.");
                        }
                        return anterior.getId();
                    }
                    if (!socio.isActivo())
                        throw new ReglaNegocioException("Un socio inactivo no puede contratar.");
                    PlanMembresia plan = planes.porId(pm, planId);
                    if (!plan.isActivo())
                        throw new ReglaNegocioException("El plan ya no está disponible.");
                    LocalDate inicio =
                            ReglasAcceso.inicioContrato(
                                    LocalDate.now(reloj), membresias.ultimo(pm, socio));
                    Membresia membresia =
                            membresias.guardar(pm, new Membresia(socio, plan, inicio));
                    pm.flush();
                    Pago pago =
                            pagos.guardar(
                                    pm,
                                    new Pago(membresia, metodo, LocalDateTime.now(reloj), token));
                    pm.flush();
                    return pago.getId();
                });
    }

    /**
     * Prepara una vista previa; el POST vuelve a validar sus condiciones actuales.
     *
     * @param socioId identidad del titular
     * @return planes disponibles con fechas y precios estimados
     */
    public List<OpcionPlan> opciones(long socioId) {
        return unidad.leer(
                pm -> {
                    Socio socio = socios.porId(pm, socioId);
                    if (!socio.isActivo())
                        throw new ReglaNegocioException("Reactive al socio antes de contratar.");
                    LocalDate inicio =
                            ReglasAcceso.inicioContrato(
                                    LocalDate.now(reloj), membresias.ultimo(pm, socio));
                    return planes
                            .listar(pm, "activo == true", Map.of(), "precio ascending")
                            .stream()
                            .map(
                                    p ->
                                            new OpcionPlan(
                                                    pm.detachCopy(p),
                                                    inicio,
                                                    inicio.plusDays(p.getDuracionDias() - 1L)))
                            .toList();
                });
    }

    /**
     * Recupera todas las relaciones usadas en el comprobante antes del cierre JDO.
     *
     * @param id identidad del pago
     * @return pago detached con contrato, socio y plan
     */
    public Pago comprobante(long id) {
        return unidad.leer(pm -> pm.detachCopy(pagos.porId(pm, id)));
    }
}
