package cl.duoc.auraarriendos.controller.v1;

import cl.duoc.auraarriendos.dto.BoletaDTO;
import cl.duoc.auraarriendos.dto.BoletaRequest;
import cl.duoc.auraarriendos.dto.ErrorResponse;
import cl.duoc.auraarriendos.service.BoletaService;
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
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Órdenes de reserva con su detalle (BOLETA y DETALLE_BOLETA)")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping
    @Operation(summary = "Listar boletas", description = "Opcionalmente filtradas por estado (pagada o rechazada).")
    @ApiResponse(responseCode = "200", description = "Listado de boletas, de la más reciente a la más antigua")
    public List<BoletaDTO> listar(@Parameter(description = "pagada o rechazada") @RequestParam(required = false) String estado) {
        return boletaService.listar(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Boleta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la boleta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BoletaDTO obtener(@Parameter(example = "1") @PathVariable Long id) {
        return boletaService.obtener(id);
    }

    @GetMapping("/usuario/{correo}")
    @Operation(summary = "Historial de compras de un usuario")
    @ApiResponse(responseCode = "200", description = "Boletas del usuario")
    public List<BoletaDTO> listarPorUsuario(@Parameter(example = "cliente@gmail.com") @PathVariable String correo) {
        return boletaService.listarPorUsuario(correo);
    }

    @PostMapping
    @Operation(summary = "Pagar una reserva (checkout)",
            description = "Valida disponibilidad, fecha y tarjeta; calcula el total en el servidor; procesa el pago "
                    + "simulado y genera la boleta. Tarjeta de prueba aprobada: 4242 4242 4242 4242; rechazada: 4000 0000 0000 0002.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Boleta generada (pagada o rechazada por el banco)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, propiedad no disponible o tarjeta inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la propiedad o el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BoletaDTO> crear(@Valid @RequestBody BoletaRequest req) {
        BoletaDTO boleta = boletaService.crear(req, req.usuarioCorreo());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(boleta.id()).toUri();
        return ResponseEntity.created(location).body(boleta);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una boleta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Boleta eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la boleta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boletaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
