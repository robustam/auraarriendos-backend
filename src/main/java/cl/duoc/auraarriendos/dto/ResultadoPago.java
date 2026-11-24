package cl.duoc.auraarriendos.dto;

/** Respuesta de la pasarela de pago simulada. */
public record ResultadoPago(boolean aprobado, String codigoAutorizacion, String motivo) {

    public static ResultadoPago aprobado(String codigo) {
        return new ResultadoPago(true, codigo, null);
    }

    public static ResultadoPago rechazado(String motivo) {
        return new ResultadoPago(false, null, motivo);
    }
}
