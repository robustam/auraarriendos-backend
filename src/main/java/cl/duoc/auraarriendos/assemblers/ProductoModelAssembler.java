package cl.duoc.auraarriendos.assemblers;

import cl.duoc.auraarriendos.controller.v2.CategoriaControllerV2;
import cl.duoc.auraarriendos.controller.v2.ProductoControllerV2;
import cl.duoc.auraarriendos.dto.ProductoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Agrega enlaces HATEOAS a una propiedad: a sí misma, al listado y a su categoría. */
@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<ProductoDTO, EntityModel<ProductoDTO>> {

    @Override
    @NonNull
    public EntityModel<ProductoDTO> toModel(@NonNull ProductoDTO producto) {
        EntityModel<ProductoDTO> modelo = EntityModel.of(producto,
                linkTo(methodOn(ProductoControllerV2.class).obtener(producto.id())).withSelfRel(),
                linkTo(methodOn(ProductoControllerV2.class).listar()).withRel("productos"));
        if (producto.categoriaId() != null) {
            modelo.add(linkTo(methodOn(CategoriaControllerV2.class).obtener(producto.categoriaId())).withRel("categoria"));
        }
        return modelo;
    }
}
