package com.gymadmin.servicio;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Casos de uso de socios, sin exponer entidades administradas a los controladores. */
@Service
public class SocioService {
    private final UnidadTrabajo unidad;
    private final SocioRepositorio socios;
    private final PersonaRepositorio personas;
    private final AsistenciaRepositorio asistencias;
    private final Clock reloj;

    /**
     * Declara las dependencias obligatorias para inyeccion por constructor.
     *
     * @param unidad transacciones compartidas
     * @param socios repositorio de socios
     * @param personas control global del DNI
     * @param asistencias visitas abiertas
     * @param reloj reloj de negocio sustituible en pruebas
     */
    public SocioService(
            UnidadTrabajo unidad,
            SocioRepositorio socios,
            PersonaRepositorio personas,
            AsistenciaRepositorio asistencias,
            Clock reloj) {
        this.unidad = unidad;
        this.socios = socios;
        this.personas = personas;
        this.asistencias = asistencias;
        this.reloj = reloj;
    }

    /**
     * Da de alta sin generar numeros contando filas.
     *
     * @param nombre nombre obligatorio
     * @param apellido apellido obligatorio
     * @param dni documento del socio
     * @param telefono contacto opcional
     * @return identidad generada al confirmar
     */
    public long crear(String nombre, String apellido, String dni, String telefono) {
        return unidad.escribir(
                pm -> {
                    Socio socio = new Socio(nombre, apellido, dni, telefono, LocalDate.now(reloj));
                    personas.exigirDniDisponible(pm, socio.getDni(), 0);
                    socios.guardar(pm, socio);
                    pm.flush();
                    return socio.getId();
                });
    }

    /**
     * Modifica datos personales conservando identidad e historiales.
     *
     * @param id identidad del socio
     * @param nombre nombre obligatorio
     * @param apellido apellido obligatorio
     * @param dni documento del socio
     * @param telefono contacto opcional
     */
    public void editar(long id, String nombre, String apellido, String dni, String telefono) {
        unidad.escribir(
                pm -> {
                    Socio socio = socios.porId(pm, id);
                    pm.retrieve(socio);
                    personas.exigirDniDisponible(pm, Validaciones.dni(dni), id);
                    socio.actualizarDatos(nombre, apellido, dni, telefono);
                    return null;
                });
    }

    /**
     * Aplica una baja logica o reactivacion, sin borrar consumos ni movimientos.
     *
     * @param id identidad del socio
     * @param activo estado solicitado
     */
    public void cambiarEstado(long id, boolean activo) {
        unidad.escribir(
                pm -> {
                    Socio socio = socios.porId(pm, id);
                    pm.retrieve(socio);
                    if (!activo && asistencias.abierta(pm, socio) != null) {
                        throw new ReglaNegocioException(
                                "Registre la salida antes de dar de baja al socio.");
                    }
                    socio.cambiarEstado(activo);
                    return null;
                });
    }

    /**
     * Recupera datos simples de un socio para edicion.
     *
     * @param id identidad del socio
     * @return socio detached sin cargar los historiales
     */
    public Socio obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(socios.porId(pm, id)));
    }

    /**
     * Busca socios sin cargar colecciones de movimientos para cada fila.
     *
     * @param texto texto o numero de socio
     * @param estado TODOS, ACTIVOS o INACTIVOS
     * @return datos separados de JDO
     */
    public List<Socio> buscar(String texto, String estado) {
        return unidad.leer(
                pm -> new ArrayList<>(pm.detachCopyAll(socios.buscar(pm, texto, estado))));
    }
}
