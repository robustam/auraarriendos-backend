package cl.duoc.auraarriendos.assemblers;

import cl.duoc.auraarriendos.controller.v2.BoletaControllerV2;
import cl.duoc.auraarriendos.controller.v2.ProductoControllerV2;
import cl.duoc.auraarriendos.dto.BoletaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Agrega enlaces HATEOAS a una boleta: a sí misma, al listado, al historial del cliente y a cada propiedad. */
@Component
public class BoletaModelAssembler implements RepresentationModelAssembler<BoletaDTO, EntityModel<BoletaDTO>> {

    @Override
    @NonNull
    public EntityModel<BoletaDTO> toModel(@NonNull BoletaDTO boleta) {
        EntityModel<BoletaDTO> modelo = EntityModel.of(boleta,
                linkTo(methodOn(BoletaControllerV2.class).obtener(boleta.id())).withSelfRel(),
                linkTo(methodOn(BoletaControllerV2.class).listar()).withRel("boletas"));
        if (boleta.usuarioCorreo() != null) {
            modelo.add(linkTo(methodOn(BoletaControllerV2.class).listarPorUsuario(boleta.usuarioCorreo()))
                    .withRel("historial-cliente"));
        }
        if (boleta.items() != null) {
            boleta.items().stream()
                    .filter(item -> item.propiedadId() != null)
                    .forEach(item -> modelo.add(linkTo(methodOn(ProductoControllerV2.class)
                            .obtener(item.propiedadId())).withRel("producto")));
        }
        return modelo;
    }
}
