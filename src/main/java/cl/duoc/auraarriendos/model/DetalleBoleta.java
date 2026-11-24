package cl.duoc.auraarriendos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla DETALLE_BOLETA: cada propiedad reservada dentro de una boleta.
 * Une BOLETA con PRODUCTO (N a 1 con cada una).
 * Guarda una copia del nombre y del precio, para que la boleta no cambie
 * si después se edita la propiedad.
 */
@Entity
@Table(name = "detalle_boleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 60)
    private String comuna;

    @Column(columnDefinition = "LONGTEXT")
    private String imagen;

    /** Precio cobrado (primer mes), con la oferta ya aplicada. */
    @Column(nullable = false)
    private Integer precio;
}
