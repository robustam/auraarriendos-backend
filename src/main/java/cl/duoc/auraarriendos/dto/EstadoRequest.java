package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Aprobar o rechazar una publicación (moderación). */
@Schema(description = "Nuevo estado de una publicación")
public record EstadoRequest(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "publicada|pendiente|rechazada", message = "Estado no válido")
        @Schema(example = "rechazada")
        String estado,

        @Size(max = 300, message = "El motivo admite máximo 300 caracteres")
        @Schema(description = "Obligatorio si se rechaza", example = "Las fotos no corresponden a la propiedad.")
        String motivo
) {
}
