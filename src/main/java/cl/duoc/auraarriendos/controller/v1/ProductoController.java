package cl.duoc.auraarriendos.controller.v1;

import cl.duoc.auraarriendos.dto.ArrendadaRequest;
import cl.duoc.auraarriendos.dto.ErrorResponse;
import cl.duoc.auraarriendos.dto.EstadoRequest;
import cl.duoc.auraarriendos.dto.ProductoDTO;
import cl.duoc.auraarriendos.dto.ProductoRequest;
import cl.duoc.auraarriendos.security.UsuarioActual;
import cl.duoc.auraarriendos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Propiedades en arriendo (PRODUCTO)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar propiedades publicadas", description = "Catálogo público de la tienda (solo estado publicada).")
    @ApiResponse(responseCode = "200", description = "Listado de propiedades publicadas")
    public List<ProductoDTO> listarPublicadas() {
        return productoService.listarPublicadas();
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todas las propiedades", description = "Incluye pendientes y rechazadas. Roles: Administrador y Vendedor.")
    @ApiResponse(responseCode = "200", description = "Listado completo")
    public List<ProductoDTO> listarTodas() {
        return productoService.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una propiedad por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedad encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoDTO obtener(@Parameter(description = "Id de la propiedad", example = "PR-1001") @PathVariable String id) {
        return productoService.obtener(id);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar propiedades publicadas de una categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedades de la categoría"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ProductoDTO> listarPorCategoria(@Parameter(example = "casa") @PathVariable String categoriaId) {
        return productoService.listarPorCategoria(categoriaId);
    }

    @GetMapping("/publicador/{correo}")
    @Operation(summary = "Listar las propiedades que publicó un usuario", description = "Usado en \"Mis publicaciones\".")
    @ApiResponse(responseCode = "200", description = "Propiedades del usuario")
    public List<ProductoDTO> listarPorPublicador(@Parameter(example = "cliente@gmail.com") @PathVariable String correo,
                                                 Authentication auth) {
        UsuarioActual.verificarDuenoOStaff(auth, correo);
        return productoService.listarPorPublicador(correo);
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Listar propiedades con stock crítico", description = "Unidades disponibles menores o iguales a la alerta.")
    @ApiResponse(responseCode = "200", description = "Propiedades con stock crítico")
    public List<ProductoDTO> listarStockCritico() {
        return productoService.listarStockCritico();
    }

    @PostMapping
    @Operation(summary = "Crear una propiedad", description = "Rol: Administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Propiedad creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o la oferta no es menor al precio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El código ya existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoRequest req) {
        return creado(productoService.crear(req));
    }

    @PostMapping("/publicaciones")
    @Operation(summary = "Publicar una propiedad (cliente)", description = "Queda en estado pendiente hasta que un administrador la apruebe.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publicación recibida"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductoDTO> publicar(@Valid @RequestBody ProductoRequest req, Authentication auth) {
        // El publicador es el usuario del token
        return creado(productoService.publicar(req, auth.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una propiedad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propiedad actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoDTO actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Aprobar o rechazar una publicación", description = "Si se rechaza, el motivo es obligatorio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o falta el motivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoDTO cambiarEstado(@PathVariable String id, @Valid @RequestBody EstadoRequest req) {
        return productoService.cambiarEstado(id, req.estado(), req.motivo());
    }

    @PatchMapping("/{id}/arrendada")
    @Operation(summary = "Marcar una propiedad como arrendada o disponible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductoDTO marcarArrendada(@PathVariable String id, @Valid @RequestBody ArrendadaRequest req) {
        return productoService.marcarArrendada(id, req.arrendada());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una propiedad", description = "No se puede eliminar si aparece en boletas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Propiedad eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La propiedad tiene boletas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProductoDTO> creado(ProductoDTO dto) {
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/productos/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(location).body(dto);
    }
}
