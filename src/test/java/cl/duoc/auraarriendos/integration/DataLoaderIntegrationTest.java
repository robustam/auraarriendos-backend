package cl.duoc.auraarriendos.integration;

import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.repository.*;
import cl.duoc.auraarriendos.util.Validaciones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica que el DataLoader (perfil dev) cargue los datos del sitio y los de DataFaker. Usa H2 en vez de MySQL. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dataloader;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("dev")
@DisplayName("Integración: carga de datos iniciales (DataLoader + DataFaker)")
class DataLoaderIntegrationTest {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BoletaRepository boletaRepository;

    @Test
    @DisplayName("Dada una base vacía, cuando arranca la app, entonces quedan categorías, propiedades, usuarios y boletas")
    void cargaDatos() {
        assertThat(categoriaRepository.count()).isEqualTo(2);
        assertThat(productoRepository.count()).isEqualTo(11);
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(7);
        assertThat(boletaRepository.count()).isEqualTo(9);
    }

    @Test
    @DisplayName("Los usuarios de prueba existen con su rol y la contraseña encriptada")
    void usuariosDePrueba() {
        var admin = usuarioRepository.findByCorreoIgnoreCase("admin@duoc.cl").orElseThrow();
        var vendedor = usuarioRepository.findByCorreoIgnoreCase("vendedor@gmail.com").orElseThrow();
        assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(vendedor.getRol()).isEqualTo(Rol.VENDEDOR);
        assertThat(new BCryptPasswordEncoder().matches("admin123", admin.getClave())).isTrue();
    }

    @Test
    @DisplayName("Los clientes generados con DataFaker tienen RUN válido y correo @gmail.com")
    void clientesFaker() {
        usuarioRepository.findByRol(Rol.CLIENTE).forEach(u -> {
            assertThat(Validaciones.runValido(u.getRun())).as(u.getRun()).isTrue();
            assertThat(Validaciones.correoPermitido(u.getCorreo())).as(u.getCorreo()).isTrue();
        });
    }
}
