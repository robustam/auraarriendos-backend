package cl.duoc.auraarriendos.controller.v2;

import cl.duoc.auraarriendos.assemblers.BoletaModelAssembler;
import cl.duoc.auraarriendos.dto.BoletaDTO;
import cl.duoc.auraarriendos.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Versión 2 de boletas con enlaces HATEOAS (HAL+JSON). Solo lectura. */
@RestController
@RequestMapping(value = "/api/v2/boletas", produces = MediaTypes.HAL_JSON_VALUE)
@Tag(name = "Boletas v2 (HATEOAS)", description = "Boletas con enlaces a sus propiedades")
public class BoletaControllerV2 {

    private final BoletaService boletaService;
    private final BoletaModelAssembler assembler;

    public BoletaControllerV2(BoletaService boletaService, BoletaModelAssembler assembler) {
        this.boletaService = boletaService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Listar boletas (HATEOAS)")
    public CollectionModel<EntityModel<BoletaDTO>> listar() {
        List<EntityModel<BoletaDTO>> boletas = boletaService.listar(null).stream().map(assembler::toModel).toList();
        return CollectionModel.of(boletas, linkTo(methodOn(BoletaControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta (HATEOAS)")
    public EntityModel<BoletaDTO> obtener(@PathVariable Long id) {
        return assembler.toModel(boletaService.obtener(id));
    }

    @GetMapping("/usuario/{correo}")
    @Operation(summary = "Historial de un usuario (HATEOAS)")
    public CollectionModel<EntityModel<BoletaDTO>> listarPorUsuario(@PathVariable String correo) {
        List<EntityModel<BoletaDTO>> boletas = boletaService.listarPorUsuario(correo).stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(boletas,
                linkTo(methodOn(BoletaControllerV2.class).listarPorUsuario(correo)).withSelfRel());
    }
}
