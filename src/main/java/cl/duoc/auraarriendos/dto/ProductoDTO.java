package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que la API devuelve de un producto (propiedad).
 * Tiene la misma forma que usa el frontend React, para que la integración sea directa.
 */
@Schema(description = "Propiedad en arriendo (producto de la tienda)")
public record ProductoDTO(
        @Schema(example = "PR-1001") String id,
        @Schema(example = "PR-1001") String codigo,
        @Schema(example = "Departamento luminoso en Providencia") String nombre,
        String descripcion,
        @Schema(description = "Precio mensual en CLP", example = "650000") Integer precio,
        @Schema(description = "Precio de oferta (opcional)", example = "590000") Integer precioOferta,
        @Schema(description = "Stock disponible", example = "3") Integer unidadesDisponibles,
        @Schema(description = "Nivel de stock crítico", example = "1") Integer alertaDisponibilidad,
        @Schema(description = "Nombre de la categoría", example = "Departamento") String categoria,
        @Schema(description = "Id de la categoría", example = "departamento") String categoriaId,
        @Schema(example = "Región Metropolitana de Santiago") String region,
        @Schema(example = "Providencia") String comuna,
        @Schema(example = "Av. Pedro de Valdivia 1204") String direccion,
        @Schema(example = "55") Integer m2,
        @Schema(example = "2") Integer dormitorios,
        @Schema(example = "1") Integer banos,
        Boolean estacionamiento,
        Boolean destacada,
        Boolean arrendada,
        LocalDateTime fechaArrendada,
        @Schema(description = "publicada, pendiente o rechazada", example = "publicada") String estado,
        String motivoRechazo,
        @Schema(example = "cliente@gmail.com") String publicadoPor,
        @Schema(description = "Primera foto de la galería", example = "/img/prop-1-1.jpg") String imagen,
        List<String> imagenes
) {
}
