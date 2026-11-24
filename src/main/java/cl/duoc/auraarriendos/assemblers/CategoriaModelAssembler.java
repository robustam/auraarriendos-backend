package cl.duoc.auraarriendos.assemblers;

import cl.duoc.auraarriendos.controller.v2.CategoriaControllerV2;
import cl.duoc.auraarriendos.controller.v2.ProductoControllerV2;
import cl.duoc.auraarriendos.dto.CategoriaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Agrega enlaces HATEOAS a una categoría: a sí misma, al listado y a sus propiedades. */
@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<CategoriaDTO, EntityModel<CategoriaDTO>> {

    @Override
    @NonNull
    public EntityModel<CategoriaDTO> toModel(@NonNull CategoriaDTO categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaControllerV2.class).obtener(categoria.id())).withSelfRel(),
                linkTo(methodOn(CategoriaControllerV2.class).listar()).withRel("categorias"),
                linkTo(methodOn(ProductoControllerV2.class).listarPorCategoria(categoria.id())).withRel("productos"));
    }
}
