package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.ResultadoPago;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Pasarela de pago SIMULADA (no cobra dinero real).
 * Tarjetas de prueba:
 *  - 4242 4242 4242 4242 → aprobada
 *  - 4000 0000 0000 0002 → rechazada por el banco
 * Está en una clase aparte para poder reemplazarla con un mock en las pruebas.
 */
@Service
public class PagoService {

    public static final String TARJETA_RECHAZADA = "4000000000000002";

    public ResultadoPago procesar(String numeroTarjeta, int monto) {
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\D", "");
        if (monto <= 0) {
            return ResultadoPago.rechazado("El monto a pagar no es válido.");
        }
        if (TARJETA_RECHAZADA.equals(digitos)) {
            return ResultadoPago.rechazado("Tarjeta rechazada por el banco emisor.");
        }
        int codigo = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return ResultadoPago.aprobado("AUT-" + codigo);
    }
}
