package cl.duoc.auraarriendos.controller.v1;

import cl.duoc.auraarriendos.dto.*;
import cl.duoc.auraarriendos.security.AuthService;
import cl.duoc.auraarriendos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve un token de acceso (15 min) y uno de renovación (7 días).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada"),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.correo(), req.clave());
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrarse como cliente", description = "Crea la cuenta (siempre rol Cliente) y deja la sesión iniciada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "RUN o correo ya registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar la sesión",
            description = "Entrega tokens nuevos. El refresh token usado queda revocado (rotación): no se puede usar dos veces.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, vencido o ya usado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.renovar(req.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.cerrarSesion(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Datos del usuario de la sesión", description = "El frontend lo usa al recargar la página para validar el token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o vencido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioDTO me(Authentication auth) {
        return usuarioService.obtenerPorCorreo(auth.getName());
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Editar mi perfil", description = "El usuario edita sus propios datos. No puede cambiar su rol ni su RUN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioDTO actualizarMe(Authentication auth, @Valid @RequestBody UsuarioRequest req) {
        UsuarioDTO actual = usuarioService.obtenerPorCorreo(auth.getName());
        UsuarioRequest seguro = new UsuarioRequest(actual.run(), req.nombre(), req.apellidos(), req.correo(), req.clave(),
                req.fechaNacimiento(), actual.tipoUsuario(), req.region(), req.comuna(), req.direccion());
        return usuarioService.actualizar(actual.id(), seguro);
    }
}
