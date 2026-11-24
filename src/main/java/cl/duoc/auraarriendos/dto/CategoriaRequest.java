package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar una categoría. */
@Schema(description = "Datos para crear o actualizar una categoría")
public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 40, message = "El nombre admite máximo 40 caracteres")
        @Schema(example = "Casa de campo")
        String nombre,

        @Size(max = 200, message = "La descripción admite máximo 200 caracteres")
        @Schema(example = "Parcelas y casas fuera de la ciudad.")
        String descripcion,

        @Schema(example = "/img/prop-6.jpg")
        String imagen
) {
}
