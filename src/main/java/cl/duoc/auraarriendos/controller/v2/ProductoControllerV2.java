package cl.duoc.auraarriendos.controller.v2;

import cl.duoc.auraarriendos.assemblers.ProductoModelAssembler;
import cl.duoc.auraarriendos.dto.ProductoDTO;
import cl.duoc.auraarriendos.dto.ProductoRequest;
import cl.duoc.auraarriendos.service.ProductoService;
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

/** Versión 2 de productos: mismas operaciones que v1, pero con enlaces HATEOAS (HAL+JSON). */
@RestController
@RequestMapping(value = "/api/v2/productos", produces = MediaTypes.HAL_JSON_VALUE)
@Tag(name = "Productos v2 (HATEOAS)", description = "Propiedades con enlaces de navegación")
public class ProductoControllerV2 {

    private final ProductoService productoService;
    private final ProductoModelAssembler assembler;

    public ProductoControllerV2(ProductoService productoService, ProductoModelAssembler assembler) {
        this.productoService = productoService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Listar propiedades publicadas (HATEOAS)")
    public CollectionModel<EntityModel<ProductoDTO>> listar() {
        List<EntityModel<ProductoDTO>> productos = productoService.listarPublicadas().stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(productos, linkTo(methodOn(ProductoControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una propiedad (HATEOAS)")
    public EntityModel<ProductoDTO> obtener(@PathVariable String id) {
        return assembler.toModel(productoService.obtener(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar propiedades de una categoría (HATEOAS)")
    public CollectionModel<EntityModel<ProductoDTO>> listarPorCategoria(@PathVariable String categoriaId) {
        List<EntityModel<ProductoDTO>> productos = productoService.listarPorCategoria(categoriaId).stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoControllerV2.class).listarPorCategoria(categoriaId)).withSelfRel());
    }

    @PostMapping
    @Operation(summary = "Crear una propiedad (HATEOAS)")
    public ResponseEntity<EntityModel<ProductoDTO>> crear(@Valid @RequestBody ProductoRequest req) {
        EntityModel<ProductoDTO> modelo = assembler.toModel(productoService.crear(req));
        return ResponseEntity.created(linkTo(methodOn(ProductoControllerV2.class)
                .obtener(modelo.getContent().id())).toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una propiedad (HATEOAS)")
    public EntityModel<ProductoDTO> actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest req) {
        return assembler.toModel(productoService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una propiedad")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
