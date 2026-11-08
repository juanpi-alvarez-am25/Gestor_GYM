package com.gymadmin.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validaciones de dominio compartidas; tambien se aplican fuera de la web. */
public final class Validaciones {
    private Validaciones() {}

    /**
     * Normaliza un texto obligatorio y limita su longitud.
     *
     * @param valor texto recibido
     * @param campo nombre utilizado en el mensaje
     * @param maximo longitud maxima del mapeo
     * @return texto sin espacios en los extremos
     * @throws ReglaNegocioException si falta o supera el limite
     */
    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > maximo) {
            throw new ReglaNegocioException(
                    campo + " es obligatorio y admite hasta " + maximo + " caracteres.");
        }
        return valor.strip();
    }

    /**
     * Quita separadores habituales del DNI antes de comparar su unicidad.
     *
     * @param valor DNI con o sin puntos, guiones o espacios
     * @return DNI normalizado de siete a nueve digitos
     * @throws ReglaNegocioException si el formato no corresponde
     */
    public static String dni(String valor) {
        String normalizado = valor == null ? "" : valor.replaceAll("[.\\s-]", "");
        if (!normalizado.matches("[0-9]{7,9}")) {
            throw new ReglaNegocioException("El DNI debe contener entre 7 y 9 dígitos.");
        }
        return normalizado;
    }

    /**
     * Asegura importes positivos y exactamente representables en centavos.
     *
     * @param valor importe de la operacion
     * @return importe con escala dos
     * @throws ReglaNegocioException si no es positivo o excede el mapeo decimal
     */
    public static BigDecimal importe(BigDecimal valor) {
        if (valor == null
                || valor.signum() <= 0
                || valor.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ReglaNegocioException("El importe debe ser positivo y menor a 100 millones.");
        }
        try {
            return valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new ReglaNegocioException("El importe admite hasta dos decimales.");
        }
    }
}
