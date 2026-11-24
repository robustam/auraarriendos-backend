package cl.duoc.auraarriendos.config;

import io.swagger.v3.oas.models.OpenAPI;
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
                                + "responde HAL+JSON con enlaces HATEOAS.")
                        .contact(new Contact().name("Equipo AuraArriendos - DSY1104")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Servidor local")));
    }
}
