package com.gymadmin.servicio;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Ingresos y salidas que serializan escrituras del mismo socio en MySQL. */
@Service
public class AccesoService {
    private final UnidadTrabajo unidad;
    private final SocioRepositorio socios;
    private final MembresiaRepositorio membresias;
    private final AsistenciaRepositorio asistencias;
    private final Clock reloj;

    /**
     * Declara la infraestructura del caso de uso.
     *
     * @param unidad transaccion y cierre
     * @param socios titular y bloqueo
     * @param membresias vigencia de los periodos
     * @param asistencias visitas y fechas consumidas
     * @param reloj reloj controlable
     */
    public AccesoService(
            UnidadTrabajo unidad,
            SocioRepositorio socios,
            MembresiaRepositorio membresias,
            AsistenciaRepositorio asistencias,
            Clock reloj) {
        this.unidad = unidad;
        this.socios = socios;
        this.membresias = membresias;
        this.asistencias = asistencias;
        this.reloj = reloj;
    }

    /**
     * Autoriza y persiste un ingreso segun las reglas R13 a R18.
     *
     * @param socioId identidad del socio
     * @return identidad de la nueva visita
     */
    public long ingresar(long socioId) {
        return unidad.escribir(
                pm -> {
                    Socio socio = socios.porId(pm, socioId);
                    pm.retrieve(socio);
                    LocalDate hoy = LocalDate.now(reloj);
                    Membresia actual = membresias.actual(pm, socio, hoy);
                    String motivo =
                            ReglasAcceso.motivoRechazo(
                                    socio.isActivo(),
                                    actual,
                                    asistencias.abierta(pm, socio) != null,
                                    asistencias.diasSemana(pm, socio, hoy),
                                    hoy);
                    if (!motivo.isEmpty()) throw new ReglaNegocioException(motivo);
                    Asistencia visita =
                            asistencias.guardar(
                                    pm, new Asistencia(socio, actual, LocalDateTime.now(reloj)));
                    pm.flush();
                    return visita.getId();
                });
    }

    /**
     * Cierra la visita aun si vencio la membresia despues del ingreso.
     *
     * @param socioId identidad del socio
     */
    public void salir(long socioId) {
        unidad.escribir(
                pm -> {
                    Socio socio = socios.porId(pm, socioId);
                    pm.retrieve(socio);
                    Asistencia abierta = asistencias.abierta(pm, socio);
                    if (abierta == null)
                        throw new ReglaNegocioException("El socio no tiene una visita abierta.");
                    abierta.registrarSalida(LocalDateTime.now(reloj));
                    return null;
                });
    }
}
