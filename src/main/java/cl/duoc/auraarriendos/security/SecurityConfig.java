package cl.duoc.auraarriendos.security;

import cl.duoc.auraarriendos.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Reglas de acceso de la API según el rol del token JWT.
 *
 *  - Público: ver catálogo (productos publicados y categorías), login, registro, renovar token y Swagger.
 *  - CLIENTE: comprar (POST boletas), ver SUS boletas, publicar su propiedad y ver SUS publicaciones.
 *  - VENDEDOR: ver todos los productos y todas las boletas (solo lectura).
 *  - ADMIN: todo lo anterior + crear, editar y eliminar productos, categorías, usuarios y boletas.
 *
 * Las reglas se revisan de arriba hacia abajo: la primera que coincide es la que se aplica.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String VENDEDOR = "VENDEDOR";

    private final JwtService jwtService;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)          // API sin cookies: el token va en el header
                .cors(Customizer.withDefaults())                 // usa la configuración de CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, 401, "Debes iniciar sesión para acceder a este recurso"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, 403, "Tu rol no tiene permiso para esta acción")))
                .authorizeHttpRequests(auth -> auth
                        // Infraestructura
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error", "/doc/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Autenticación
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro",
                                "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/me").authenticated()

                        // Productos: rutas de gestión antes que la ruta pública /{id}
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/todos", "/api/v1/productos/stock-critico")
                        .hasAnyRole(ADMIN, VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/publicador/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/publicaciones").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos", "/api/v1/productos/**",
                                "/api/v2/productos", "/api/v2/productos/**").permitAll()

                        // Categorías: lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias", "/api/v1/categorias/**",
                                "/api/v2/categorias", "/api/v2/categorias/**").permitAll()

                        // Boletas: el cliente compra y ve las suyas (se verifica el dueño en el controlador)
                        .requestMatchers(HttpMethod.POST, "/api/v1/boletas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/usuario/**", "/api/v1/boletas/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas", "/api/v2/boletas", "/api/v2/boletas/**")
                        .hasAnyRole(ADMIN, VENDEDOR)

                        // Todo lo demás de la API (crear, editar, eliminar, usuarios): solo administrador
                        .requestMatchers("/api/**").hasRole(ADMIN)
                        .anyRequest().denyAll())
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void escribirError(HttpServletResponse res, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), ErrorResponse.de(estado, mensaje));
    }
}
