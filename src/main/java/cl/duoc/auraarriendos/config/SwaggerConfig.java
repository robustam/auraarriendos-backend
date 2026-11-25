package cl.duoc.auraarriendos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Documentación de la API con Swagger (OpenAPI). Disponible en /doc/swagger-ui.html */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 2026 AuraArriendos")
                        .version("1.0")
                        .description("Documentación de la API REST de AuraArriendos: propiedades (productos), "
                                + "categorías, usuarios y boletas. La versión v1 responde JSON simple y la v2 "
                                + "responde HAL+JSON con enlaces HATEOAS. "
                                + "Las rutas privadas requieren un token JWT (botón Authorize).")
                        .contact(new Contact().name("Equipo AuraArriendos - DSY1104")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Servidor local")))
                // Botón "Authorize" de Swagger: se pega el accessToken obtenido en /api/v1/auth/login
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Token de acceso JWT. Obténlo en POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
