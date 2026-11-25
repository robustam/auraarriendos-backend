package cl.duoc.auraarriendos.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta una vez en cada petición: si trae "Authorization: Bearer <token>" y el token
 * es válido, deja al usuario autenticado con su rol (ROLE_ADMIN, ROLE_VENDEDOR o ROLE_CLIENTE).
 * Si no trae token o no es válido, la petición sigue como anónima y SecurityConfig decide si la deja pasar.
 * No es un @Component a propósito: se crea en SecurityConfig, así las pruebas @WebMvcTest no lo cargan.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.validar(header.substring(7).trim(), JwtService.TIPO_ACCESO).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Claims claims) {
        String rol = claims.get("rol", String.class);
        var auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
