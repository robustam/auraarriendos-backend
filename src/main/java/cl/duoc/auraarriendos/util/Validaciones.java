package cl.duoc.auraarriendos.util;

import java.text.Normalizer;
import java.time.YearMonth;
import java.util.List;

/**
 * Reglas de validación del negocio que se repiten en varios servicios.
 * Son las mismas que valida el frontend, pero aquí el servidor tiene la última palabra.
 */
public final class Validaciones {

    /** Dominios de correo permitidos por el cliente. */
    public static final List<String> DOMINIOS_PERMITIDOS = List.of("duoc.cl", "profesor.duoc.cl", "gmail.com");

    private Validaciones() {
    }

    /** RUN chileno sin puntos ni guion, con dígito verificador correcto (módulo 11). */
    public static boolean runValido(String run) {
        if (run == null) {
            return false;
        }
        String limpio = run.trim().toUpperCase();
        if (!limpio.matches("^[0-9]{6,8}[0-9K]$")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        return calcularDv(cuerpo) == dv;
    }

    /** Calcula el dígito verificador de un RUN (0-9 o K). */
    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return Character.forDigit(resto, 10);
    }

    /** El correo debe terminar en uno de los dominios permitidos. */
    public static boolean correoPermitido(String correo) {
        if (correo == null || !correo.contains("@")) {
            return false;
        }
        String dominio = correo.substring(correo.lastIndexOf('@') + 1).toLowerCase();
        return DOMINIOS_PERMITIDOS.contains(dominio);
    }

    /** Algoritmo de Luhn: detecta números de tarjeta mal escritos. */
    public static boolean tarjetaValida(String numero) {
        if (numero == null) {
            return false;
        }
        String digitos = numero.replaceAll("\\s|-", "");
        if (!digitos.matches("^[0-9]{13,19}$")) {
            return false;
        }
        int suma = 0;
        boolean duplicar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (duplicar) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    /** Vencimiento con formato MM/AA y en el futuro (o el mes actual). */
    public static boolean vencimientoValido(String vencimiento, YearMonth hoy) {
        if (vencimiento == null || !vencimiento.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            return false;
        }
        int mes = Integer.parseInt(vencimiento.substring(0, 2));
        int anio = 2000 + Integer.parseInt(vencimiento.substring(3));
        return !YearMonth.of(anio, mes).isBefore(hoy);
    }

    /** Convierte un texto en un identificador legible: "Casa de Campo" -> "casa-de-campo". */
    public static String slug(String texto) {
        String base = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return base.isEmpty() ? "categoria" : base;
    }
}
