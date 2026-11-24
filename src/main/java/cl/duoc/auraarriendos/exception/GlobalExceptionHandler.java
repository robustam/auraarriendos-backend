package cl.duoc.auraarriendos.exception;

import cl.duoc.auraarriendos.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte los errores en respuestas JSON claras, con el código HTTP correcto.
 * Así el frontend siempre recibe { estado, mensaje, errores } y puede mostrarlo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.de(404, ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(ex.getEstado()).body(ErrorResponse.de(ex.getEstado().value(), ex.getMessage()));
    }

    /** Errores de @Valid: se devuelve un mensaje por cada campo inválido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Revisa los datos enviados", errores, LocalDateTime.now()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> peticionInvalida(Exception ex) {
        String mensaje = ex instanceof IllegalArgumentException ? ex.getMessage() : "El cuerpo de la petición no es un JSON válido";
        return ResponseEntity.badRequest().body(ErrorResponse.de(400, mensaje));
    }

    /** Ruta inexistente (404) o método/tipo no soportado (405, 415...): se respeta el código original. */
    @ExceptionHandler({NoResourceFoundException.class, ErrorResponseException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> errorHttp(Exception ex) {
        int estado = ex instanceof org.springframework.web.ErrorResponse er ? er.getStatusCode().value() : 400;
        String mensaje = estado == 404 ? "La ruta solicitada no existe" : "La petición no es válida: " + ex.getMessage();
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado, mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.de(500, "Ocurrió un error en el servidor"));
    }
}
