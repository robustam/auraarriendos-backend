package cl.duoc.auraarriendos.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación rompe una regla del negocio
 * (ej: oferta mayor que el precio, correo repetido, propiedad sin stock).
 * Responde 400 (datos inválidos) o 409 (conflicto con lo que ya existe).
 */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estado;

    public ReglaNegocioException(String mensaje) {
        this(HttpStatus.BAD_REQUEST, mensaje);
    }

    public ReglaNegocioException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
