package cl.duoc.auraarriendos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla BOLETA: una reserva pagada (o rechazada) por un usuario.
 * Pertenece a un USUARIO (N a 1) y tiene uno o más DETALLE_BOLETA (1 a N).
 */
@Entity
@Table(name = "boleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número correlativo visible en la boleta (desde 20260001). */
    @Column(nullable = false, unique = true)
    private Long numero;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** pagada / rechazada */
    @Column(nullable = false, length = 20)
    private String estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /* Datos del cliente tal como los escribió al pagar (copia para la boleta). */
    @Column(nullable = false, length = 50)
    private String clienteNombre;

    @Column(nullable = false, length = 100)
    private String clienteApellidos;

    @Column(nullable = false, length = 100)
    private String clienteCorreo;

    /* Domicilio del cliente. */
    @Column(nullable = false, length = 300)
    private String calle;

    @Column(length = 50)
    private String departamento;

    @Column(nullable = false, length = 80)
    private String region;

    @Column(nullable = false, length = 60)
    private String comuna;

    @Column(length = 300)
    private String indicaciones;

    /** Fecha de inicio del arriendo. */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /** Solo los últimos 4 dígitos de la tarjeta (ej: "•••• 4242"). */
    @Column(nullable = false, length = 20)
    private String tarjeta;

    @Column(nullable = false)
    private Integer total;

    @Column(length = 20)
    private String codigoAutorizacion;

    @Column(length = 200)
    private String motivoRechazo;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleBoleta> detalles = new ArrayList<>();

    /** Agrega un detalle y mantiene la relación en ambos sentidos. */
    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.setBoleta(this);
        detalles.add(detalle);
    }
}
