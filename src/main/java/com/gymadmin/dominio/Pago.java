package com.gymadmin.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.jdo.annotations.*;

/** Pago completo por periodo, protegido contra reenvios y duplicados (R09 a R12). */
@PersistenceCapable(table = "PAGOS", detachable = "true")
public class Pago {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private long id;

    @Unique(name = "UQ_PAGO_MEMBRESIA")
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "MEMBRESIA_ID", allowsNull = "false")
    private Membresia membresia;

    @Column(allowsNull = "false")
    private LocalDateTime fechaHora;

    @Column(allowsNull = "false", jdbcType = "DECIMAL", length = 10, scale = 2)
    private BigDecimal importe;

    @Column(allowsNull = "false", jdbcType = "VARCHAR", length = 24)
    private MetodoPago metodo;

    @Unique(name = "UQ_PAGO_COMPROBANTE")
    @Column(allowsNull = "false", length = 40)
    private String numeroComprobante;

    @Unique(name = "UQ_PAGO_OPERACION")
    @Column(allowsNull = "false", length = 36)
    private String claveOperacion;

    /** Constructor reservado a JDO. */
    protected Pago() {}

    /**
     * Registra el importe del servidor, nunca un precio recibido del navegador.
     *
     * @param membresia periodo pagado completamente
     * @param metodo medio recibido por recepcion
     * @param fechaHora instante local del reloj de negocio
     * @param claveOperacion UUID de la solicitud para idempotencia
     */
    public Pago(
            Membresia membresia,
            MetodoPago metodo,
            LocalDateTime fechaHora,
            String claveOperacion) {
        this.membresia = Objects.requireNonNull(membresia);
        if (metodo == null) throw new ReglaNegocioException("Seleccione el método de pago.");
        this.metodo = metodo;
        this.fechaHora = Objects.requireNonNull(fechaHora);
        this.importe = Validaciones.importe(membresia.getPrecioAcordado());
        try {
            this.claveOperacion =
                    UUID.fromString(Objects.requireNonNull(claveOperacion)).toString();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ReglaNegocioException(
                    "La clave de operación no es válida. Vuelva a abrir el formulario.");
        }
        this.numeroComprobante = "GYM-" + UUID.randomUUID();
    }

    /**
     * @return identidad del pago
     */
    public long getId() {
        return id;
    }

    /**
     * @return periodo pagado
     */
    public Membresia getMembresia() {
        return membresia;
    }

    /**
     * @return fecha y hora local del movimiento
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * @return importe registrado
     */
    public BigDecimal getImporte() {
        return importe;
    }

    /**
     * @return medio de pago
     */
    public MetodoPago getMetodo() {
        return metodo;
    }

    /**
     * @return numero interno unico; no es factura fiscal
     */
    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    /**
     * @return token idempotente de la solicitud
     */
    public String getClaveOperacion() {
        return claveOperacion;
    }
}
