package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** Lo que la API devuelve de un usuario. Nunca incluye la contraseña. */
@Schema(description = "Usuario del sistema (sin contraseña)")
public record UsuarioDTO(
        @Schema(example = "3") Long id,
        @Schema(example = "333333333") String run,
        @Schema(example = "Matías") String nombre,
        @Schema(example = "Fernández Lira") String apellidos,
        @Schema(example = "cliente@gmail.com") String correo,
        LocalDate fechaNacimiento,
        @Schema(description = "Administrador, Vendedor o Cliente", example = "Cliente") String tipoUsuario,
        @Schema(example = "Región Metropolitana de Santiago") String region,
        @Schema(example = "La Florida") String comuna,
        @Schema(example = "Av. Vicuña Mackenna 8900") String direccion
) {
}
