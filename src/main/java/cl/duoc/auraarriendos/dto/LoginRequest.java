package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") @Schema(example = "admin@duoc.cl") String correo,
        @NotBlank(message = "La contraseña es obligatoria") @Schema(example = "admin123") String clave
) {
}
