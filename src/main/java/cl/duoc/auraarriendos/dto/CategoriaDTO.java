package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Lo que la API devuelve de una categoría. */
@Schema(description = "Categoría o tipo de inmueble")
public record CategoriaDTO(
        @Schema(example = "casa") String id,
        @Schema(example = "Casa") String nombre,
        @Schema(example = "Casas con patio o jardín, ideales para familias y mascotas.") String descripcion,
        @Schema(example = "/img/prop-2.jpg") String imagen,
        @Schema(description = "Cantidad de propiedades de la categoría", example = "4") long cantidadProductos
) {
}
