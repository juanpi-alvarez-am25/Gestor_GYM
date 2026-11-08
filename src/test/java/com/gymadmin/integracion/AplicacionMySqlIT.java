package com.gymadmin.integracion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gymadmin.dominio.*;
import com.gymadmin.repositorio.*;
import com.gymadmin.servicio.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;

/**
 * Pruebas reales de JDO, MySQL, transacciones y renderizado MVC. Borra datos SOLO de un esquema
 * cuyo nombre termine en _test.
 */
@SpringBootTest(
        properties = {
            "gym.db.url=${GYM_TEST_DB_URL:jdbc:mysql://localhost:3306/gymadmin_test}",
            "gym.db.user=${GYM_TEST_DB_USER:gymadmin}",
            "gym.db.password=${GYM_TEST_DB_PASSWORD}"
        })
@AutoConfigureMockMvc
class AplicacionMySqlIT {
    @Autowired SocioService socios;
    @Autowired PersonalService personal;
    @Autowired MembresiaService membresias;
    @Autowired AccesoService accesos;
    @Autowired ConsultaService consultas;
    @Autowired UnidadTrabajo unidad;
    @Autowired PlanRepositorio planes;
    @Autowired SocioRepositorio sociosRepo;
    @Autowired MembresiaRepositorio membresiasRepo;
    @Autowired MockMvc web;
    @MockitoBean Clock reloj;
    @MockitoSpyBean PagoRepositorio pagos;

    @Value("${gym.db.url}")
    String url;

    @Value("${gym.db.user}")
    String usuario;

    @Value("${gym.db.password}")
    String clave;

    private final ZoneId zona = ZoneId.of("America/Argentina/Buenos_Aires");

    @BeforeEach
    void prepararBaseExclusiva() throws Exception {
        String sinParametros = url.split("\\?", 2)[0];
        String esquema = sinParametros.substring(sinParametros.lastIndexOf('/') + 1);
        if (!esquema.matches("[a-zA-Z0-9_]+_test"))
            throw new IllegalStateException(
                    "Las pruebas requieren un esquema exclusivo terminado en _test.");
        when(reloj.getZone()).thenReturn(zona);
        fijar("2026-11-02T10:00:00");
        try (Connection c = DriverManager.getConnection(url, usuario, clave);
                Statement s = c.createStatement()) {
            for (String tabla :
                    List.of(
                            "PAGOS",
                            "ASISTENCIAS",
                            "MEMBRESIAS",
                            "ENTRENADORES",
                            "EMPLEADOS",
                            "SOCIOS",
                            "PERSONAS")) {
                s.executeUpdate("DELETE FROM " + tabla);
            }
            s.executeUpdate(
                    "UPDATE PLANES SET PRECIO=18000.00, DURACIONDIAS=30, LIMITESEMANAL=2, ACTIVO=1"
                        + " WHERE NOMBRE='Bronce'");
        }
    }

    private void fijar(String fecha) {
        when(reloj.instant()).thenReturn(LocalDateTime.parse(fecha).atZone(zona).toInstant());
    }

    private long alta() {
        return socios.crear("Ana", "Prueba", "40.123.456", "299123456");
    }

    private long plan(String nombre) {
        return consultas.planes().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private long contratar(long id) {
        return membresias.contratar(
                id, plan("Bronce"), MetodoPago.EFECTIVO, UUID.randomUUID().toString());
    }

    private int contar(String tabla) throws Exception {
        try (Connection c = DriverManager.getConnection(url, usuario, clave);
                Statement s = c.createStatement();
                ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            r.next();
            return r.getInt(1);
        }
    }

    @Test
    void altaEdicionBusquedaBajaYUnicidadGlobal() {
        long id = alta();
        String numero = socios.obtener(id).getNumeroSocio();
        assertEquals(id, socios.buscar("40.123.456", "TODOS").get(0).getId());
        assertEquals(id, socios.buscar(numero, "TODOS").get(0).getId());
        socios.editar(id, "Ana", "Actualizada", "40123456", "299999999");
        assertEquals(numero, socios.obtener(id).getNumeroSocio());
        assertEquals("Actualizada", socios.obtener(id).getApellido());
        socios.cambiarEstado(id, false);
        assertThrows(
                ReglaNegocioException.class,
                () ->
                        personal.crear(
                                "Otro", "Prueba", "40123456", "", TipoEmpleado.LIMPIEZA, null, ""));
        assertThrows(ReglaNegocioException.class, () -> contratar(id));
        socios.cambiarEstado(id, true);
        assertTrue(socios.obtener(id).isActivo());
    }

    @Test
    void herenciaRecuperaClasesYCamposDeTodasLasTablas() {
        personal.crear("Rita", "Recepcion", "40123457", "", TipoEmpleado.RECEPCIONISTA, null, "");
        personal.crear("Luis", "Limpieza", "40123458", "", TipoEmpleado.LIMPIEZA, null, "");
        long id =
                personal.crear(
                        "Eva",
                        "Entrena",
                        "40123459",
                        "",
                        TipoEmpleado.ENTRENADOR,
                        Especialidad.FUERZA,
                        "8 a 16");
        personal.editar(
                id,
                "Eva",
                "Editada",
                "40123459",
                "",
                TipoEmpleado.ENTRENADOR,
                Especialidad.FUNCIONAL,
                "9 a 17");
        Entrenador e = assertInstanceOf(Entrenador.class, personal.obtener(id));
        assertEquals("Editada", e.getApellido());
        assertEquals(Especialidad.FUNCIONAL, e.getEspecialidad());
        assertEquals("9 a 17", e.getHorario());
        assertEquals(3, personal.listar(null).size());
        assertEquals(1, personal.listar(TipoEmpleado.ENTRENADOR).size());
        assertThrows(
                ReglaNegocioException.class,
                () ->
                        personal.editar(
                                id,
                                "Eva",
                                "Editada",
                                "40123459",
                                "",
                                TipoEmpleado.LIMPIEZA,
                                null,
                                ""));
        assertEquals(TipoEmpleado.ENTRENADOR, personal.obtener(id).getTipo());
    }

    @Test
    void pagoCompletoReenvioYRelacionesFueraDelContexto() throws Exception {
        long id = alta(), plan = plan("Bronce");
        String token = UUID.randomUUID().toString();
        long pagoId = membresias.contratar(id, plan, MetodoPago.TARJETA, token);
        assertEquals(pagoId, membresias.contratar(id, plan, MetodoPago.TARJETA, token));
        Pago pago = membresias.comprobante(pagoId);
        assertEquals("Ana", pago.getMembresia().getSocio().getNombre());
        assertEquals("Bronce", pago.getMembresia().getPlan().getNombre());
        assertEquals(new BigDecimal("18000.00"), pago.getImporte());
        assertEquals(LocalDate.of(2026, 11, 2), pago.getMembresia().getInicio());
        assertEquals(LocalDate.of(2026, 12, 1), pago.getMembresia().getVencimiento());
        assertEquals(1, contar("PAGOS"));
        assertEquals(1, contar("MEMBRESIAS"));
        web.perform(get("/pagos/" + pagoId))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "No válido como factura fiscal")));
        web.perform(get("/socios/" + id)).andExpect(status().isOk());
    }

    @Test
    void fallaDelPagoRevierteUnContratoYaInsertado() throws Exception {
        long id = alta();
        doAnswer(
                        inv -> {
                            PersistenceManager pm = inv.getArgument(0);
                            assertEquals(
                                    1,
                                    membresiasRepo.historial(pm, sociosRepo.porId(pm, id)).size());
                            throw new JDOFatalDataStoreException(
                                    "Falla deliberada de prueba al guardar el pago");
                        })
                .when(pagos)
                .guardar(any(PersistenceManager.class), any(Pago.class));
        assertThrows(PersistenciaException.class, () -> contratar(id));
        assertEquals(0, contar("MEMBRESIAS"));
        assertEquals(0, contar("PAGOS"));
    }

    @Test
    void renovacionAnticipadaUnaFuturaYVencidaSinSolapamientos() {
        long id = alta();
        contratar(id);
        long segundo = contratar(id);
        assertEquals(
                LocalDate.of(2026, 12, 2),
                membresias.comprobante(segundo).getMembresia().getInicio());
        assertThrows(ReglaNegocioException.class, () -> contratar(id));
        fijar("2027-01-05T10:00:00");
        long tercero = contratar(id);
        assertEquals(
                LocalDate.of(2027, 1, 5),
                membresias.comprobante(tercero).getMembresia().getInicio());
    }

    @Test
    void cambioDelCatalogoConservaPrecioDuracionYLimite() {
        long id = alta();
        long pago = contratar(id);
        long planId = plan("Bronce");
        unidad.escribir(
                pm -> {
                    planes.porId(pm, planId)
                            .actualizarCondiciones(new BigDecimal("25000.00"), 10, 3);
                    return null;
                });
        Membresia m = membresias.comprobante(pago).getMembresia();
        assertEquals(new BigDecimal("18000.00"), m.getPrecioAcordado());
        assertEquals(30, m.getDuracionDias());
        assertEquals(2, m.getLimiteSemanal());
    }

    @Test
    void limiteReingresoReinicioSemanalYVisitasAbiertas() {
        long id = alta();
        contratar(id);
        fijar("2026-11-03T10:00:00");
        accesos.ingresar(id);
        assertThrows(ReglaNegocioException.class, () -> accesos.ingresar(id));
        assertThrows(ReglaNegocioException.class, () -> socios.cambiarEstado(id, false));
        fijar("2026-11-03T11:00:00");
        accesos.salir(id);
        fijar("2026-11-05T10:00:00");
        accesos.ingresar(id);
        fijar("2026-11-05T11:00:00");
        accesos.salir(id);
        fijar("2026-11-05T12:00:00");
        accesos.ingresar(id);
        fijar("2026-11-05T13:00:00");
        accesos.salir(id);
        fijar("2026-11-06T10:00:00");
        assertThrows(ReglaNegocioException.class, () -> accesos.ingresar(id));
        fijar("2026-11-09T10:00:00");
        accesos.ingresar(id);
        assertEquals(4, consultas.ficha(id).getAsistencias().size());
    }

    @Test
    void salidaLuegoDeVencimientoYBajaConservanHistorial() {
        long id = alta();
        long planId = plan("Bronce");
        unidad.escribir(
                pm -> {
                    planes.porId(pm, planId).actualizarCondiciones(new BigDecimal("100.00"), 1, 2);
                    return null;
                });
        contratar(id);
        accesos.ingresar(id);
        fijar("2026-11-03T10:00:00");
        accesos.salir(id);
        assertThrows(ReglaNegocioException.class, () -> accesos.ingresar(id));
        socios.cambiarEstado(id, false);
        var ficha = consultas.ficha(id);
        assertEquals(1, ficha.getPagos().size());
        assertEquals(1, ficha.getAsistencias().size());
        assertFalse(ficha.getResumen().getSocio().isActivo());
        assertThrows(ReglaNegocioException.class, () -> accesos.salir(id));
    }

    @Test
    void contratosConcurrentesConLaMismaClaveDevuelvenUnSoloPago() throws Exception {
        long id = alta(), plan = plan("Bronce");
        String token = UUID.randomUUID().toString();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Callable<Long> tarea =
                    () -> {
                        inicio.await();
                        return membresias.contratar(id, plan, MetodoPago.EFECTIVO, token);
                    };
            Future<Long> uno = pool.submit(tarea), dos = pool.submit(tarea);
            inicio.countDown();
            assertEquals(uno.get(15, TimeUnit.SECONDS), dos.get(15, TimeUnit.SECONDS));
            assertEquals(1, contar("PAGOS"));
            assertEquals(1, contar("MEMBRESIAS"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void ingresosConcurrentesNoDejanDosVisitasAbiertas() throws Exception {
        long id = alta();
        contratar(id);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Callable<Boolean> tarea =
                    () -> {
                        inicio.await();
                        try {
                            accesos.ingresar(id);
                            return true;
                        } catch (ReglaNegocioException | PersistenciaException ex) {
                            return false;
                        }
                    };
            Future<Boolean> uno = pool.submit(tarea), dos = pool.submit(tarea);
            inicio.countDown();
            assertNotEquals(uno.get(15, TimeUnit.SECONDS), dos.get(15, TimeUnit.SECONDS));
            assertEquals(1, contar("ASISTENCIAS"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void pantallasFormulariosValidacionCsrfYPrecioDelServidor() throws Exception {
        long id = alta();
        long plan = plan("Bronce");
        for (String ruta :
                List.of(
                        "/",
                        "/socios",
                        "/socios/nuevo",
                        "/socios/" + id,
                        "/socios/" + id + "/editar",
                        "/personal",
                        "/personal/nuevo",
                        "/planes",
                        "/recepcion",
                        "/vencimientos",
                        "/socios/" + id + "/contratar",
                        "/css/gym.css",
                        "/webjars/bootstrap/5.3.8/css/bootstrap.min.css")) {
            web.perform(get(ruta)).andExpect(status().isOk());
        }
        web.perform(
                        post("/socios")
                                .with(csrf())
                                .param("nombre", "")
                                .param("apellido", "")
                                .param("dni", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "nombre", "apellido", "dni"));
        web.perform(post("/socios").param("nombre", "Atacante")).andExpect(status().isForbidden());
        String token = UUID.randomUUID().toString();
        web.perform(
                        post("/socios/" + id + "/contratar")
                                .with(csrf())
                                .param("planId", "" + plan)
                                .param("metodo", "EFECTIVO")
                                .param("claveOperacion", token)
                                .param("importe", "1.00"))
                .andExpect(status().is3xxRedirection());
        assertEquals(
                new BigDecimal("18000.00"), consultas.ficha(id).getPagos().get(0).getImporte());
        web.perform(get("/socios/" + id)).andExpect(status().isOk());
        web.perform(get("/recepcion")).andExpect(status().isOk());
        web.perform(get("/vencimientos").param("estado", "VIGENTES")).andExpect(status().isOk());
        long entrenador =
                personal.crear(
                        "Eva",
                        "Entrena",
                        "40123459",
                        "",
                        TipoEmpleado.ENTRENADOR,
                        Especialidad.FUERZA,
                        "8 a 16");
        web.perform(get("/personal")).andExpect(status().isOk());
        web.perform(get("/personal/" + entrenador + "/editar")).andExpect(status().isOk());
    }

    @Test
    void jarEjecutableRecuperaLosDatosEnOtroProceso() throws Exception {
        long id = alta();
        contratar(id);
        accesos.ingresar(id);
        int puerto;
        try (ServerSocket socket = new ServerSocket(0)) {
            puerto = socket.getLocalPort();
        }
        Path log = Path.of("target", "prueba-jar.log").toAbsolutePath();
        ProcessBuilder builder =
                new ProcessBuilder(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-jar",
                        System.getProperty("gym.test.jar"));
        builder.environment()
                .putAll(
                        Map.of(
                                "GYM_DB_URL",
                                url,
                                "GYM_DB_USER",
                                usuario,
                                "GYM_DB_PASSWORD",
                                clave,
                                "GYM_PORT",
                                String.valueOf(puerto)));
        Process proceso = builder.redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            HttpClient cliente =
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
            HttpRequest solicitud =
                    HttpRequest.newBuilder(
                                    URI.create("http://127.0.0.1:" + puerto + "/socios/" + id))
                            .timeout(Duration.ofSeconds(2))
                            .GET()
                            .build();
            long fin = System.nanoTime() + TimeUnit.SECONDS.toNanos(40);
            while (System.nanoTime() < fin) {
                if (!proceso.isAlive())
                    fail("El JAR terminó antes de iniciar; revisar target/prueba-jar.log.");
                try {
                    HttpResponse<String> respuesta =
                            cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
                    if (respuesta.statusCode() == 200) {
                        assertTrue(respuesta.body().contains("40123456"));
                        assertTrue(respuesta.body().contains("Bronce"));
                        assertTrue(respuesta.body().contains("EFECTIVO"));
                        assertTrue(respuesta.body().contains("Visita abierta"));
                        return;
                    }
                } catch (java.io.IOException ex) {
                    /* Todavia esta iniciando el servidor. */
                }
                Thread.sleep(200);
            }
            fail("El JAR no respondió en 40 segundos; revisar target/prueba-jar.log.");
        } finally {
            proceso.destroy();
            if (!proceso.waitFor(10, TimeUnit.SECONDS)) proceso.destroyForcibly();
        }
    }
}
