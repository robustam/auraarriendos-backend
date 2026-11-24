package cl.duoc.auraarriendos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend React (que corre en otro puerto) llame a la API.
 * Los orígenes permitidos se leen de la variable CORS_ORIGENES (ver application.properties).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.origenes}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .maxAge(3600);
    }
}
