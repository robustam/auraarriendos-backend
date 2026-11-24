package cl.duoc.auraarriendos.controller.v2;

import cl.duoc.auraarriendos.assemblers.CategoriaModelAssembler;
import cl.duoc.auraarriendos.dto.CategoriaDTO;
import cl.duoc.auraarriendos.dto.CategoriaRequest;
import cl.duoc.auraarriendos.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Versión 2 de categorías con enlaces HATEOAS (HAL+JSON). */
@RestController
@RequestMapping(value = "/api/v2/categorias", produces = MediaTypes.HAL_JSON_VALUE)
@Tag(name = "Categorías v2 (HATEOAS)", description = "Categorías con enlaces de navegación")
public class CategoriaControllerV2 {

    private final CategoriaService categoriaService;
    private final CategoriaModelAssembler assembler;

    public CategoriaControllerV2(CategoriaService categoriaService, CategoriaModelAssembler assembler) {
        this.categoriaService = categoriaService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Listar categorías (HATEOAS)")
    public CollectionModel<EntityModel<CategoriaDTO>> listar() {
        List<EntityModel<CategoriaDTO>> categorias = categoriaService.listar().stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(categorias, linkTo(methodOn(CategoriaControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría (HATEOAS)")
    public EntityModel<CategoriaDTO> obtener(@PathVariable String id) {
        return assembler.toModel(categoriaService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear una categoría (HATEOAS)")
    public ResponseEntity<EntityModel<CategoriaDTO>> crear(@Valid @RequestBody CategoriaRequest req) {
        EntityModel<CategoriaDTO> modelo = assembler.toModel(categoriaService.crear(req));
        return ResponseEntity.created(linkTo(methodOn(CategoriaControllerV2.class)
                .obtener(modelo.getContent().id())).toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría (HATEOAS)")
    public EntityModel<CategoriaDTO> actualizar(@PathVariable String id, @Valid @RequestBody CategoriaRequest req) {
        return assembler.toModel(categoriaService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
