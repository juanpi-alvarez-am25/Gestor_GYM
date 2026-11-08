package com.gymadmin.servicio;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;
import com.gymadmin.servicio.vista.*;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import javax.jdo.PersistenceManager;

/** Lecturas para las pantallas; prepara relaciones antes de cerrar la unidad JDO. */
@Service
public class ConsultaService {
    private final UnidadTrabajo unidad;
    private final SocioRepositorio socios;
    private final MembresiaRepositorio membresias;
    private final PagoRepositorio pagos;
    private final AsistenciaRepositorio asistencias;
    private final PlanRepositorio planes;
    private final Clock reloj;

    /**
     * Declara las consultas de las vistas.
     *
     * @param unidad administracion de recursos
     * @param socios datos personales
     * @param membresias contratos
     * @param pagos movimientos de pago
     * @param asistencias visitas
     * @param planes catalogo
     * @param reloj fecha de negocio
     */
    public ConsultaService(
            UnidadTrabajo unidad,
            SocioRepositorio socios,
            MembresiaRepositorio membresias,
            PagoRepositorio pagos,
            AsistenciaRepositorio asistencias,
            PlanRepositorio planes,
            Clock reloj) {
        this.unidad = unidad;
        this.socios = socios;
        this.membresias = membresias;
        this.pagos = pagos;
        this.asistencias = asistencias;
        this.planes = planes;
        this.reloj = reloj;
    }

    /**
     * Carga los historiales solo para la ficha solicitada.
     *
     * @param id identidad del socio
     * @return ficha con objetos detached y colecciones materializadas
     */
    public FichaSocio ficha(long id) {
        return unidad.leer(
                pm -> {
                    Socio socio = socios.porId(pm, id);
                    return new FichaSocio(
                            resumen(pm, socio),
                            new ArrayList<>(pm.detachCopyAll(membresias.historial(pm, socio))),
                            new ArrayList<>(
                                    pm.detachCopyAll(
                                            pagos.listar(
                                                    pm,
                                                    "membresia.socio == :socio",
                                                    Map.of("socio", socio),
                                                    "fechaHora descending"))),
                            new ArrayList<>(
                                    pm.detachCopyAll(
                                            asistencias.listar(
                                                    pm,
                                                    "socio == :socio",
                                                    Map.of("socio", socio),
                                                    "ingreso descending"))));
                });
    }

    /**
     * Prepara estados para recepcion sin cargar todos los movimientos de cada socio.
     *
     * @param texto busqueda por nombre, documento o numero
     * @return socios encontrados y autorizacion calculada hoy
     */
    public List<ResumenSocio> recepcion(String texto) {
        return unidad.leer(
                pm -> socios.buscar(pm, texto, "TODOS").stream().map(s -> resumen(pm, s)).toList());
    }

    /**
     * Filtra el vencimiento del periodo actual o del ultimo cuando no hay vigente.
     *
     * @param estado VENCIDAS, VIGENTES, POR_VENCER o SIN_PLAN
     * @param dias horizonte de 1 a 90 dias
     * @return socios activos que cumplen el filtro
     */
    public List<ResumenSocio> vencimientos(String estado, int dias) {
        if (dias < 1 || dias > 90)
            throw new ReglaNegocioException("El horizonte debe ser de 1 a 90 días.");
        LocalDate hoy = LocalDate.now(reloj);
        return unidad.leer(
                pm ->
                        socios.buscar(pm, "", "ACTIVOS").stream()
                                .map(s -> resumen(pm, s))
                                .filter(
                                        r -> {
                                            return switch (estado) {
                                                case "VENCIDAS" ->
                                                        r.getActual() == null
                                                                && r.getUltimo() != null
                                                                && r.getUltimo()
                                                                        .getVencimiento()
                                                                        .isBefore(hoy);
                                                case "VIGENTES" -> r.getActual() != null;
                                                case "POR_VENCER" ->
                                                        r.getActual() != null
                                                                && !r.getActual()
                                                                        .getVencimiento()
                                                                        .isAfter(
                                                                                hoy.plusDays(dias));
                                                case "SIN_PLAN" -> r.getUltimo() == null;
                                                default -> false;
                                            };
                                        })
                                .toList());
    }

    /**
     * @return catalogo separado del contexto y ordenado por precio
     */
    public List<PlanMembresia> planes() {
        return unidad.leer(
                pm ->
                        new ArrayList<>(
                                pm.detachCopyAll(
                                        planes.listar(pm, "", Map.of(), "precio ascending"))));
    }

    /**
     * @return indicadores del panel: activos, visitas abiertas y vencimientos en siete dias
     */
    public Map<String, Integer> panel() {
        return unidad.leer(
                pm -> {
                    LocalDate hoy = LocalDate.now(reloj);
                    List<Socio> activos = socios.buscar(pm, "", "ACTIVOS");
                    int proximos = 0;
                    for (Socio socio : activos) {
                        Membresia actual = membresias.actual(pm, socio, hoy);
                        if (actual != null && !actual.getVencimiento().isAfter(hoy.plusDays(7)))
                            proximos++;
                    }
                    return Map.of(
                            "activos",
                            activos.size(),
                            "abiertas",
                            asistencias.listar(pm, "salida == null", Map.of(), "").size(),
                            "proximos",
                            proximos);
                });
    }

    private ResumenSocio resumen(PersistenceManager pm, Socio socio) {
        LocalDate hoy = LocalDate.now(reloj);
        Membresia actual = membresias.actual(pm, socio, hoy);
        // La pantalla necesita el ultimo periodo; se consulta sin cargar colecciones del socio.
        Membresia ultimo = membresias.ultimo(pm, socio);
        boolean abierta = asistencias.abierta(pm, socio) != null;
        Set<LocalDate> dias = asistencias.diasSemana(pm, socio, hoy);
        String motivo = ReglasAcceso.motivoRechazo(socio.isActivo(), actual, abierta, dias, hoy);
        return new ResumenSocio(
                pm.detachCopy(socio),
                actual == null ? null : pm.detachCopy(actual),
                ultimo == null ? null : pm.detachCopy(ultimo),
                abierta,
                dias.size(),
                motivo);
    }
}
