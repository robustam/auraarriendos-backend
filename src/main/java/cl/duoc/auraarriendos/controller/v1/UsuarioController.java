package cl.duoc.auraarriendos.controller.v1;

import cl.duoc.auraarriendos.dto.ErrorResponse;
import cl.duoc.auraarriendos.dto.UsuarioDTO;
import cl.duoc.auraarriendos.dto.UsuarioRequest;
import cl.duoc.auraarriendos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administradores, vendedores y clientes (USUARIO)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "La contraseña nunca se devuelve.")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios")
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioDTO obtener(@Parameter(example = "1") @PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @GetMapping("/correo/{correo}")
    @Operation(summary = "Buscar un usuario por correo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioDTO obtenerPorCorreo(@Parameter(example = "cliente@gmail.com") @PathVariable String correo) {
        return usuarioService.obtenerPorCorreo(correo);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario", description = "Valida RUN (dígito verificador), dominio del correo y duplicados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "RUN o correo ya registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioRequest req) {
        UsuarioDTO creado = usuarioService.crear(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = "El RUN no se modifica. Contraseña en blanco = se conserva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioDTO actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return usuarioService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "No se puede eliminar si tiene boletas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El usuario tiene boletas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
