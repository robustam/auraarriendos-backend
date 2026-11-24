package cl.duoc.auraarriendos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Datos para crear o editar un usuario. Las reglas son las del formulario de registro. */
@Schema(description = "Datos para crear o actualizar un usuario")
public record UsuarioRequest(
        @NotBlank(message = "El RUN es obligatorio")
        @Pattern(regexp = "^[0-9]{6,8}[0-9Kk]$", message = "El RUN va sin puntos ni guion (7 a 9 caracteres)")
        @Schema(example = "190110222")
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre admite máximo 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$", message = "El nombre solo admite letras")
        @Schema(example = "Camila")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos admiten máximo 100 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$", message = "Los apellidos solo admiten letras")
        @Schema(example = "Rojas Pérez")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo admite máximo 100 caracteres")
        @Schema(example = "camila.rojas@gmail.com")
        String correo,

        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres")
        @Schema(description = "Obligatoria al crear; al editar, en blanco conserva la actual", example = "clave123")
        String clave,

        LocalDate fechaNacimiento,

        @Schema(description = "Administrador, Vendedor o Cliente", example = "Cliente")
        String tipoUsuario,

        @NotBlank(message = "La región es obligatoria")
        @Schema(example = "Región Metropolitana de Santiago")
        String region,

        @NotBlank(message = "La comuna es obligatoria")
        @Schema(example = "Ñuñoa")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 300, message = "La dirección admite máximo 300 caracteres")
        @Schema(example = "Irarrázaval 3000")
        String direccion
) {
}
