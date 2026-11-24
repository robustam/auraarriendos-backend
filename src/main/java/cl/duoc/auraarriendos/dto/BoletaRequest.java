package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos del checkout: qué propiedades se reservan, quién paga y con qué tarjeta.
 * El total NO viene del frontend: lo calcula el servidor con los precios de la base de datos.
 */
@Schema(description = "Datos para pagar una reserva y generar la boleta")
public record BoletaRequest(
        @Schema(description = "Correo del usuario que compra", example = "cliente@gmail.com")
        String usuarioCorreo,

        @NotEmpty(message = "Debe reservar al menos una propiedad")
        @Schema(example = "[\"PR-1001\"]")
        List<String> propiedadIds,

        @NotNull(message = "Faltan los datos del cliente") @Valid
        Cliente cliente,

        @NotNull(message = "Falta la dirección") @Valid
        Direccion direccion,

        @NotNull(message = "La fecha de inicio del arriendo es obligatoria")
        @Schema(example = "2026-12-01")
        LocalDate fechaInicio,

        @NotNull(message = "Faltan los datos de la tarjeta") @Valid
        Tarjeta tarjeta
) {
    public record Cliente(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 50) @Schema(example = "Matías") String nombre,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 100) @Schema(example = "Fernández Lira") String apellidos,
            @NotBlank(message = "El correo es obligatorio") @Email @Size(max = 100) @Schema(example = "cliente@gmail.com") String correo) {
    }

    public record Direccion(
            @NotBlank(message = "La calle es obligatoria") @Size(max = 300) @Schema(example = "Av. Vicuña Mackenna 8900") String calle,
            @Size(max = 50) String departamento,
            @NotBlank(message = "La región es obligatoria") @Schema(example = "Región Metropolitana de Santiago") String region,
            @NotBlank(message = "La comuna es obligatoria") @Schema(example = "La Florida") String comuna,
            @Size(max = 300) String indicaciones) {
    }

    public record Tarjeta(
            @NotBlank(message = "El titular es obligatorio") @Size(max = 100) @Schema(example = "Matías Fernández") String titular,
            @NotBlank(message = "El número de tarjeta es obligatorio") @Schema(example = "4242 4242 4242 4242") String numero,
            @NotBlank(message = "El vencimiento es obligatorio") @Schema(example = "12/28") String vencimiento,
            @NotBlank(message = "El CVV es obligatorio") @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV tiene 3 o 4 dígitos") @Schema(example = "123") String cvv) {
    }
}
