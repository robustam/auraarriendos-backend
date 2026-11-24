package cl.duoc.auraarriendos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla CATEGORIA: tipo de inmueble (Casa, Departamento...).
 * Una categoría agrupa muchos productos (relación 1 a N con PRODUCTO).
 */
@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Categoría o tipo de inmueble")
public class Categoria {

    /** Identificador legible generado desde el nombre (ej: "casa"). Se usa en las URLs del frontend. */
    @Id
    @Column(length = 60)
    @Schema(description = "Identificador de la categoría", example = "casa")
    private String id;

    @Column(nullable = false, unique = true, length = 40)
    @Schema(description = "Nombre de la categoría", example = "Casa")
    private String nombre;

    @Column(length = 200)
    @Schema(description = "Descripción corta", example = "Casas con patio o jardín, ideales para familias.")
    private String descripcion;

    /** Ruta de la imagen (/img/...) o imagen en base64. */
    @Column(columnDefinition = "LONGTEXT")
    @Schema(description = "Ruta o imagen en base64", example = "/img/prop-2.jpg")
    private String imagen;
}
