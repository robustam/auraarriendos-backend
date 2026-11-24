package cl.duoc.auraarriendos.model;

import cl.duoc.auraarriendos.util.ListaJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla PRODUCTO: en AuraArriendos el "producto" de la tienda es una propiedad en arriendo.
 * Cada producto pertenece a una CATEGORIA (relación N a 1).
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    /** Identificador correlativo con formato PR-#### (lo usa el frontend en /propiedades/:id). */
    @Id
    @Column(length = 20)
    private String id;

    /** Código interno visible en el panel; es único. */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Precio mensual en pesos chilenos. */
    @Column(nullable = false)
    private Integer precio;

    /** Precio rebajado opcional; siempre menor que el precio normal (regla de negocio). */
    private Integer precioOferta;

    /** Stock: unidades disponibles para reservar. Baja en 1 con cada boleta pagada. */
    @Column(nullable = false)
    private Integer unidadesDisponibles;

    /** Nivel de stock crítico: si unidadesDisponibles <= alerta, aparece en "Stock crítico". */
    private Integer alertaDisponibilidad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 80)
    private String region;

    @Column(nullable = false, length = 60)
    private String comuna;

    @Column(nullable = false, length = 300)
    private String direccion;

    @Column(nullable = false)
    private Integer m2;

    private Integer dormitorios;

    private Integer banos;

    @Builder.Default
    private Boolean estacionamiento = false;

    @Builder.Default
    private Boolean destacada = false;

    @Builder.Default
    private Boolean arrendada = false;

    private LocalDateTime fechaArrendada;

    /** publicada / pendiente / rechazada */
    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 300)
    private String motivoRechazo;

    /** Correo del usuario que publicó la propiedad (para "Mis publicaciones"). */
    @Column(length = 100)
    private String publicadoPor;

    /** Galería de fotos guardada como una lista JSON en una sola columna. */
    @Convert(converter = ListaJsonConverter.class)
    @Column(columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> imagenes = new ArrayList<>();

    /** Precio que se cobra: el de oferta si es válido, si no el normal. */
    public int precioFinal() {
        if (precioOferta != null && precioOferta > 0 && precioOferta < precio) {
            return precioOferta;
        }
        return precio;
    }

    /** Se puede reservar si está publicada, no está arrendada y tiene stock. */
    public boolean disponibleParaReservar() {
        return "publicada".equals(estado)
                && !Boolean.TRUE.equals(arrendada)
                && unidadesDisponibles != null && unidadesDisponibles > 0;
    }
}
