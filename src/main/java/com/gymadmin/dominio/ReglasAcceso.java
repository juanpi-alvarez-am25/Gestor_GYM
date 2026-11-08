package com.gymadmin.dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

/** Reglas puras de calendario y acceso; no dependen del servidor ni de JDO. */
public final class ReglasAcceso {
    private ReglasAcceso() {}

    /**
     * Determina el inicio de semana con lunes como primer dia (R13).
     *
     * @param fecha dia local del gimnasio
     * @return lunes de esa semana
     */
    public static LocalDate lunes(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Calcula el primer dia de un nuevo periodo o rechaza una segunda renovacion futura.
     *
     * @param hoy dia local actual
     * @param ultimo ultimo contrato ordenado por vencimiento, o null
     * @return fecha inicial sin solapamientos
     */
    public static LocalDate inicioContrato(LocalDate hoy, Membresia ultimo) {
        if (ultimo == null || ultimo.getVencimiento().isBefore(hoy)) return hoy;
        if (ultimo.getInicio().isAfter(hoy)) {
            throw new ReglaNegocioException(
                    "Ya existe una renovación futura; espere al inicio de ese período.");
        }
        return ultimo.getVencimiento().plusDays(1);
    }

    /**
     * Informa la primera causa que impide ingresar, aplicando el reingreso del mismo dia.
     *
     * @param activo estado del socio
     * @param actual contrato aplicable hoy, o null
     * @param visitaAbierta si hay un ingreso sin salida
     * @param diasSemana fechas distintas ya consumidas por el socio en la semana
     * @param hoy fecha local actual
     * @return motivo de rechazo, o cadena vacia si puede ingresar
     */
    public static String motivoRechazo(
            boolean activo,
            Membresia actual,
            boolean visitaAbierta,
            Set<LocalDate> diasSemana,
            LocalDate hoy) {
        if (!activo) return "Socio inactivo. Reactive su registro para operar.";
        if (visitaAbierta) return "Tiene una visita abierta. Registre primero la salida.";
        if (actual == null || !actual.aplicaEn(hoy))
            return "No tiene una membresía vigente para hoy.";
        if (actual.getLimiteSemanal() > 0
                && !diasSemana.contains(hoy)
                && diasSemana.size() >= actual.getLimiteSemanal()) {
            return "Alcanzó el límite de días distintos de esta semana.";
        }
        return "";
    }
}
