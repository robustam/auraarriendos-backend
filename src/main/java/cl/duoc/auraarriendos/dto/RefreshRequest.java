package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Token de renovación")
public record RefreshRequest(
        @NotBlank(message = "El refresh token es obligatorio") String refreshToken
) {
}
