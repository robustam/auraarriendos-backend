package cl.duoc.auraarriendos.controller.v1;

import cl.duoc.auraarriendos.dto.CategoriaDTO;
import cl.duoc.auraarriendos.dto.CategoriaRequest;
import cl.duoc.auraarriendos.dto.ErrorResponse;
import cl.duoc.auraarriendos.service.CategoriaService;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Tipos de inmueble (CATEGORIA)")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías con la cantidad de propiedades de cada una.")
    @ApiResponse(responseCode = "200", description = "Listado de categorías")
    public List<CategoriaDTO> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoriaDTO obtener(@Parameter(description = "Id de la categoría", example = "casa") @PathVariable String id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear una categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaRequest req) {
        CategoriaDTO creada = categoriaService.crear(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoriaDTO actualizar(@PathVariable String id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría", description = "No se puede eliminar si tiene propiedades asociadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La categoría tiene propiedades",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
