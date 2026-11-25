package cl.duoc.auraarriendos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Registro de cada refresh token entregado. Permite la ROTACIÓN:
 * cada vez que se usa un refresh token se revoca y se entrega uno nuevo,
 * así un token robado deja de servir apenas el dueño renueva su sesión.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    /** Identificador único del token (claim "jti" del JWT). */
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String correo;

    @Column(nullable = false)
    private Instant expira;

    @Column(nullable = false)
    private boolean revocado;
}
