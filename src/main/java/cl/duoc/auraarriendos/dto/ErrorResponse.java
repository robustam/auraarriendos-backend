package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de los errores de la API, para que el frontend los muestre fácilmente. */
@Schema(description = "Respuesta de error")
public record ErrorResponse(
        @Schema(example = "404") int estado,
        @Schema(example = "No existe la propiedad PR-9999") String mensaje,
        @Schema(description = "Errores por campo (solo en validaciones)") Map<String, String> errores,
        LocalDateTime fecha
) {
    public static ErrorResponse de(int estado, String mensaje) {
        return new ErrorResponse(estado, mensaje, Map.of(), LocalDateTime.now());
    }
}
