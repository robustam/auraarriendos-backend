package cl.duoc.auraarriendos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Genera y verifica los tokens JWT.
 *  - Token de ACCESO: dura poco (15 min). Se envía en cada petición: Authorization: Bearer ...
 *  - Token de RENOVACIÓN (refresh): dura más (7 días). Solo sirve para pedir un token de acceso nuevo.
 * Ambos van firmados con HMAC-SHA256 usando una clave secreta que viene de la variable JWT_SECRET.
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESO = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final Duration duracionAcceso;
    private final Duration duracionRefresh;

    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.minutos-acceso:15}") long minutosAcceso,
                      @Value("${app.jwt.dias-refresh:7}") long diasRefresh) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionAcceso = Duration.ofMinutes(minutosAcceso);
        this.duracionRefresh = Duration.ofDays(diasRefresh);
    }

    /** Token de acceso con el correo (subject) y el rol del usuario. */
    public String generarAcceso(String correo, String rol) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .claim("tipo", TIPO_ACCESO)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionAcceso)))
                .signWith(clave)
                .compact();
    }

    /** Token de renovación. Su id (jti) se guarda en la base de datos para poder revocarlo. */
    public String generarRefresh(String correo, String jti) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(correo)
                .claim("tipo", TIPO_REFRESH)
                .id(jti)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionRefresh)))
                .signWith(clave)
                .compact();
    }

    /** Verifica firma y expiración. Devuelve vacío si el token no es válido o no es del tipo pedido. */
    public Optional<Claims> validar(String token, String tipoEsperado) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return tipoEsperado.equals(claims.get("tipo", String.class)) ? Optional.of(claims) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long segundosAcceso() {
        return duracionAcceso.toSeconds();
    }

    public Instant expiracionRefresh() {
        return Instant.now().plus(duracionRefresh);
    }
}
