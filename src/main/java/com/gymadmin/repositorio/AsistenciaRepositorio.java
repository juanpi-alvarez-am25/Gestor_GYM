package com.gymadmin.repositorio;

import com.gymadmin.dominio.Asistencia;
import com.gymadmin.dominio.ReglasAcceso;
import com.gymadmin.dominio.Socio;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.PersistenceManager;

/** Consultas de visitas abiertas y de fechas consumidas por semana. */
@Repository
public class AsistenciaRepositorio extends RepositorioJdo<Asistencia> {
    /** Configura el tipo Asistencia. */
    public AsistenciaRepositorio() {
        super(Asistencia.class);
    }

    /**
     * Busca la visita que todavia no tiene salida.
     *
     * @param pm administrador actual
     * @param socio titular
     * @return visita abierta o null
     */
    public Asistencia abierta(PersistenceManager pm, Socio socio) {
        List<Asistencia> lista =
                listar(
                        pm,
                        "socio == :socio && salida == null",
                        Map.of("socio", socio),
                        "ingreso descending");
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Cuenta fechas distintas, incluyendo visitas cerradas y periodos anteriores.
     *
     * @param pm administrador actual
     * @param socio titular
     * @param hoy fecha local del gimnasio
     * @return conjunto de dias consumidos entre lunes y el lunes siguiente
     */
    public Set<LocalDate> diasSemana(PersistenceManager pm, Socio socio, LocalDate hoy) {
        LocalDate lunes = ReglasAcceso.lunes(hoy);
        return listar(
                        pm,
                        "socio == :socio && ingreso >= :desde && ingreso < :hasta",
                        Map.of(
                                "socio",
                                socio,
                                "desde",
                                lunes.atStartOfDay(),
                                "hasta",
                                lunes.plusDays(7).atStartOfDay()),
                        "")
                .stream()
                .map(a -> a.getIngreso().toLocalDate())
                .collect(Collectors.toSet());
    }
}
