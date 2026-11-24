package cl.duoc.auraarriendos.exception;

/** Se lanza cuando se busca un registro que no existe. Responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
