package com.gymadmin.servicio;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** ABM de personal con herencia real y bajas logicas. */
@Service
public class PersonalService {
    private final UnidadTrabajo unidad;
    private final PersonalRepositorio personal;
    private final PersonaRepositorio personas;

    /**
     * Declara dependencias para el contenedor Spring.
     *
     * @param unidad transacciones
     * @param personal repositorio polimorfico
     * @param personas unicidad del DNI en toda la jerarquia
     */
    public PersonalService(
            UnidadTrabajo unidad, PersonalRepositorio personal, PersonaRepositorio personas) {
        this.unidad = unidad;
        this.personal = personal;
        this.personas = personas;
    }

    /**
     * Crea un empleado o su especializacion Entrenador.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento
     * @param telefono contacto opcional
     * @param tipo funcion
     * @param especialidad obligatoria solo para entrenamiento
     * @param horario obligatorio solo para entrenamiento
     * @return identidad generada
     */
    public long crear(
            String nombre,
            String apellido,
            String dni,
            String telefono,
            TipoEmpleado tipo,
            Especialidad especialidad,
            String horario) {
        return unidad.escribir(
                pm -> {
                    Empleado empleado =
                            tipo == TipoEmpleado.ENTRENADOR
                                    ? new Entrenador(
                                            nombre, apellido, dni, telefono, especialidad, horario)
                                    : new Empleado(nombre, apellido, dni, telefono, tipo);
                    personas.exigirDniDisponible(pm, empleado.getDni(), 0);
                    personal.guardar(pm, empleado);
                    pm.flush();
                    return empleado.getId();
                });
    }

    /**
     * Edita sin convertir la clase persistente de un registro existente.
     *
     * @param id identidad del personal
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento
     * @param telefono contacto opcional
     * @param tipo funcion compatible con la clase
     * @param especialidad area del entrenador
     * @param horario horario del entrenador
     */
    public void editar(
            long id,
            String nombre,
            String apellido,
            String dni,
            String telefono,
            TipoEmpleado tipo,
            Especialidad especialidad,
            String horario) {
        unidad.escribir(
                pm -> {
                    Empleado empleado = personal.porId(pm, id);
                    pm.retrieve(empleado);
                    personas.exigirDniDisponible(pm, Validaciones.dni(dni), id);
                    empleado.actualizarDatos(nombre, apellido, dni, telefono);
                    empleado.cambiarTipo(tipo);
                    if (empleado instanceof Entrenador entrenador)
                        entrenador.actualizarEntrenamiento(especialidad, horario);
                    return null;
                });
    }

    /**
     * Conserva al empleado al cambiar su estado.
     *
     * @param id identidad del empleado
     * @param activo estado administrativo
     */
    public void cambiarEstado(long id, boolean activo) {
        unidad.escribir(
                pm -> {
                    personal.porId(pm, id).cambiarEstado(activo);
                    return null;
                });
    }

    /**
     * Recupera la clase concreta y sus datos comunes y propios.
     *
     * @param id identidad
     * @return empleado detached, posiblemente Entrenador
     */
    public Empleado obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(personal.porId(pm, id)));
    }

    /**
     * Lista personal sin ocultar registros inactivos.
     *
     * @param tipo funcion a filtrar, o null para todas
     * @return empleados separados del contexto
     */
    public List<Empleado> listar(TipoEmpleado tipo) {
        return unidad.leer(
                pm ->
                        new ArrayList<>(
                                pm.detachCopyAll(
                                        personal.listar(
                                                pm,
                                                tipo == null ? "" : "tipo == :tipo",
                                                tipo == null ? Map.of() : Map.of("tipo", tipo),
                                                "apellido ascending"))));
    }
}
