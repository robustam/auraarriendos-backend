package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Datos para crear o editar un producto (propiedad). Las reglas son las de la pauta. */
@Schema(description = "Datos para crear o actualizar una propiedad")
public record ProductoRequest(
        @Size(min = 3, max = 30, message = "El código debe tener entre 3 y 30 caracteres")
        @Schema(description = "Código único (si se omite, se usa el id generado)", example = "PR-1011")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        @Schema(example = "Casa con patio en La Reina")
        String nombre,

        @Size(max = 500, message = "La descripción admite máximo 500 caracteres")
        String descripcion,

        @NotBlank(message = "La categoría es obligatoria")
        @Schema(description = "Nombre o id de la categoría", example = "Casa")
        String categoria,

        @NotBlank(message = "La región es obligatoria")
        @Schema(example = "Región Metropolitana de Santiago")
        String region,

        @NotBlank(message = "La comuna es obligatoria")
        @Schema(example = "La Reina")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 300, message = "La dirección admite máximo 300 caracteres")
        @Schema(example = "Av. Larraín 6100")
        String direccion,

        @NotNull(message = "La superficie es obligatoria")
        @Min(value = 1, message = "La superficie debe ser al menos 1 m²")
        @Schema(example = "95")
        Integer m2,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        @Schema(example = "890000")
        Integer precio,

        @Min(value = 0, message = "El precio de oferta no puede ser negativo")
        @Schema(description = "Debe ser menor que el precio", example = "850000")
        Integer precioOferta,

        @NotNull(message = "Las unidades disponibles son obligatorias")
        @Min(value = 0, message = "Las unidades disponibles no pueden ser negativas")
        @Schema(example = "1")
        Integer unidadesDisponibles,

        @Min(value = 0, message = "La alerta de stock no puede ser negativa")
        @Schema(example = "1")
        Integer alertaDisponibilidad,

        @Min(value = 0, message = "Los dormitorios no pueden ser negativos")
        @Schema(example = "3")
        Integer dormitorios,

        @Min(value = 0, message = "Los baños no pueden ser negativos")
        @Schema(example = "2")
        Integer banos,

        Boolean estacionamiento,
        Boolean destacada,
        Boolean arrendada,

        @Schema(description = "publicada, pendiente o rechazada (por defecto publicada)", example = "publicada")
        String estado,

        String motivoRechazo,

        @Schema(description = "Correo de quien publica", example = "admin@duoc.cl")
        String publicadoPor,

        @Schema(description = "Foto principal (ruta o base64)", example = "/img/prop-1.jpg")
        String imagen,

        List<String> imagenes
) {
}
