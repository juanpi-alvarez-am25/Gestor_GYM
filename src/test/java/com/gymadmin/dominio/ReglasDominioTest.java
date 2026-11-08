package com.gymadmin.dominio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Casos de calendario, invariantes e historicos de la especificacion R01 a R17. */
class ReglasDominioTest {
    private final LocalDate jueves = LocalDate.of(2026, 11, 5);
    private final Socio socio = new Socio("Ana", "Prueba", "40123456", "", jueves);

    private PlanMembresia plan(int duracion, int limite) {
        return new PlanMembresia("Plan de prueba", new BigDecimal("18000.00"), duracion, limite);
    }

    @ParameterizedTest
    @ValueSource(strings = {"40123456", "40.123.456", "40 123 456", "40-123-456"})
    void normalizaDniAntesDeComparar(String dni) {
        assertEquals("40123456", Validaciones.dni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ABC12345", "123456", "1234567890"})
    void rechazaDniInvalido(String dni) {
        assertThrows(ReglaNegocioException.class, () -> Validaciones.dni(dni));
    }

    @Test
    void obligaDatosPersonales() {
        assertThrows(
                ReglaNegocioException.class,
                () -> new Socio(" ", "Prueba", "40123456", "", jueves));
        assertThrows(
                ReglaNegocioException.class, () -> new Socio("Ana", "", "40123456", "", jueves));
    }

    @Test
    void importeExigePositivoYCentavos() {
        for (String valor : new String[] {"0", "-1", "1.001", "100000000"}) {
            assertThrows(
                    ReglaNegocioException.class, () -> Validaciones.importe(new BigDecimal(valor)));
        }
        assertEquals(new BigDecimal("12.30"), Validaciones.importe(new BigDecimal("12.3")));
    }

    @Test
    void periodoDeUnDiaIncluyeAmbosExtremos() {
        Membresia m = new Membresia(socio, plan(1, 2), jueves);
        assertEquals(jueves, m.getVencimiento());
        assertTrue(m.aplicaEn(jueves));
        assertFalse(m.aplicaEn(jueves.minusDays(1)));
        assertFalse(m.aplicaEn(jueves.plusDays(1)));
    }

    @Test
    void duracionTreintaDiasNoAgregaUnDiaExtra() {
        Membresia m = new Membresia(socio, plan(30, 2), jueves);
        assertEquals(jueves.plusDays(29), m.getVencimiento());
    }

    @Test
    void cambiosDelPlanNoReescribenElContrato() {
        PlanMembresia p = plan(30, 2);
        Membresia m = new Membresia(socio, p, jueves);
        p.actualizarCondiciones(new BigDecimal("22000.00"), 10, 3);
        assertEquals(new BigDecimal("18000.00"), m.getPrecioAcordado());
        assertEquals(30, m.getDuracionDias());
        assertEquals(2, m.getLimiteSemanal());
    }

    @Test
    void renuevaAlDiaSiguienteDelVencimientoVigente() {
        Membresia m = new Membresia(socio, plan(30, 2), jueves);
        assertEquals(m.getVencimiento().plusDays(1), ReglasAcceso.inicioContrato(jueves, m));
    }

    @Test
    void primeraContratacionYRenovacionVencidaEmpiezanHoy() {
        assertEquals(jueves, ReglasAcceso.inicioContrato(jueves, null));
        assertEquals(
                jueves,
                ReglasAcceso.inicioContrato(
                        jueves, new Membresia(socio, plan(1, 2), jueves.minusDays(3))));
    }

    @Test
    void rechazaSegundaRenovacionFutura() {
        assertThrows(
                ReglaNegocioException.class,
                () ->
                        ReglasAcceso.inicioContrato(
                                jueves, new Membresia(socio, plan(30, 2), jueves.plusDays(1))));
    }

    @Test
    void semanaSiempreEmpiezaEnLunes() {
        assertEquals(LocalDate.of(2026, 11, 2), ReglasAcceso.lunes(jueves));
        assertEquals(LocalDate.of(2026, 11, 2), ReglasAcceso.lunes(LocalDate.of(2026, 11, 8)));
        assertEquals(LocalDate.of(2026, 11, 9), ReglasAcceso.lunes(LocalDate.of(2026, 11, 9)));
    }

    @Test
    void broncePermiteReingresarElMismoDiaPeroNoUnTercero() {
        Membresia m = new Membresia(socio, plan(30, 2), jueves.minusDays(3));
        Set<LocalDate> dias = Set.of(jueves.minusDays(2), jueves);
        assertEquals("", ReglasAcceso.motivoRechazo(true, m, false, dias, jueves));
        assertFalse(ReglasAcceso.motivoRechazo(true, m, false, dias, jueves.plusDays(1)).isEmpty());
    }

    @Test
    void plataYLibreRespetanSusLimites() {
        Set<LocalDate> dias = Set.of(jueves.minusDays(3), jueves.minusDays(2), jueves.minusDays(1));
        assertFalse(
                ReglasAcceso.motivoRechazo(
                                true,
                                new Membresia(socio, plan(30, 3), jueves.minusDays(3)),
                                false,
                                dias,
                                jueves)
                        .isEmpty());
        assertEquals(
                "",
                ReglasAcceso.motivoRechazo(
                        true,
                        new Membresia(socio, plan(30, 0), jueves.minusDays(3)),
                        false,
                        dias,
                        jueves));
    }

    @Test
    void inactivoSinPlanYVisitaAbiertaImpidenIngresar() {
        Membresia m = new Membresia(socio, plan(30, 0), jueves);
        assertFalse(ReglasAcceso.motivoRechazo(false, m, false, Set.of(), jueves).isEmpty());
        assertFalse(ReglasAcceso.motivoRechazo(true, null, false, Set.of(), jueves).isEmpty());
        assertFalse(ReglasAcceso.motivoRechazo(true, m, true, Set.of(), jueves).isEmpty());
    }

    @Test
    void salidaPosteriorPuedeCerrarUnaVisitaConContratoVencido() {
        Asistencia visita =
                new Asistencia(
                        socio, new Membresia(socio, plan(1, 2), jueves), jueves.atTime(22, 0));
        assertThrows(
                ReglaNegocioException.class, () -> visita.registrarSalida(jueves.atTime(22, 0)));
        visita.registrarSalida(jueves.plusDays(1).atTime(1, 0));
        assertFalse(visita.isAbierta());
        assertThrows(
                ReglaNegocioException.class,
                () -> visita.registrarSalida(jueves.plusDays(1).atTime(2, 0)));
    }

    @Test
    void asistenciaExigeElMismoTitularYFechaValida() {
        Membresia m = new Membresia(socio, plan(1, 2), jueves);
        Socio otro = new Socio("Leo", "Otro", "40123457", "", jueves);
        assertThrows(
                ReglaNegocioException.class, () -> new Asistencia(otro, m, jueves.atTime(10, 0)));
        assertThrows(
                ReglaNegocioException.class,
                () -> new Asistencia(socio, m, jueves.plusDays(1).atTime(10, 0)));
    }

    @Test
    void entrenadorNoCambiaDeClaseAlEditar() {
        Entrenador entrenador =
                new Entrenador("Leo", "Prueba", "40123457", "", Especialidad.FUERZA, "8 a 16");
        assertEquals(TipoEmpleado.ENTRENADOR, entrenador.getTipo());
        assertThrows(
                ReglaNegocioException.class, () -> entrenador.cambiarTipo(TipoEmpleado.LIMPIEZA));
        assertThrows(
                ReglaNegocioException.class,
                () -> new Empleado("Ana", "Prueba", "40123456", "", TipoEmpleado.ENTRENADOR));
        assertThrows(
                ReglaNegocioException.class,
                () -> entrenador.actualizarEntrenamiento(null, "8 a 16"));
    }
}
