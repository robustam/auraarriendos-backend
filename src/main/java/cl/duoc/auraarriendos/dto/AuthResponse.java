package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tokens de sesión y datos del usuario")
public record AuthResponse(
        @Schema(description = "JWT de acceso (va en Authorization: Bearer ...)") String accessToken,
        @Schema(description = "JWT para renovar la sesión") String refreshToken,
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "Segundos de vida del token de acceso", example = "900") long expiraEn,
        UsuarioDTO usuario
) {
}
