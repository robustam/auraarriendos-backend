package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Marcar una propiedad como arrendada o volver a dejarla disponible. */
@Schema(description = "Cambia la disponibilidad de una propiedad")
public record ArrendadaRequest(
        @NotNull(message = "Indique true o false")
        @Schema(example = "true")
        Boolean arrendada
) {
}
