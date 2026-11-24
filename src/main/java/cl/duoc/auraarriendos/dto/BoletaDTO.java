package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Lo que la API devuelve de una boleta (orden), con sus detalles. */
@Schema(description = "Boleta de una reserva pagada o rechazada")
public record BoletaDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "20260001") Long numero,
        LocalDateTime fecha,
        @Schema(description = "pagada o rechazada", example = "pagada") String estado,
        Cliente cliente,
        Direccion direccion,
        LocalDate fechaInicio,
        @Schema(example = "•••• 4242") String tarjeta,
        @Schema(example = "AUT-583920") String codigoAutorizacion,
        String motivoRechazo,
        @Schema(example = "cliente@gmail.com") String usuarioCorreo,
        List<Item> items,
        @Schema(example = "420000") Integer total
) {
    /** Datos del cliente impresos en la boleta. */
    public record Cliente(String nombre, String apellidos, String correo) {
    }

    /** Domicilio del cliente. */
    public record Direccion(String calle, String departamento, String region, String comuna, String indicaciones) {
    }

    /** Una línea de la boleta (DETALLE_BOLETA). */
    public record Item(Long id, String propiedadId, String nombre, String comuna, String imagen, Integer precio) {
    }
}
