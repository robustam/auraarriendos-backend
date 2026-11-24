package cl.duoc.auraarriendos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del backend de AuraArriendos.
 * Swagger queda disponible en: http://localhost:8080/doc/swagger-ui.html
 */
@SpringBootApplication
public class AuraArriendosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuraArriendosApplication.class, args);
    }
}
