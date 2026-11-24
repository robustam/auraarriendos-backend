package cl.duoc.auraarriendos.integration;

import cl.duoc.auraarriendos.model.Categoria;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prueba de integración: API + servicios + base de datos H2 (perfil test). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración: flujo completo de compra con base de datos H2")
class FlujoCompraIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BoletaRepository boletaRepository;

    @BeforeEach
    void preparar() {
        boletaRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
        Categoria depto = categoriaRepository.save(Categoria.builder().id("departamento").nombre("Departamento").build());
        productoRepository.save(Producto.builder().id("PR-1001").codigo("PR-1001").nombre("Depto Providencia")
                .precio(650000).precioOferta(600000).unidadesDisponibles(2).alertaDisponibilidad(1).categoria(depto)
                .region("Región Metropolitana de Santiago").comuna("Providencia").direccion("Av. Pedro de Valdivia 1204")
                .m2(55).dormitorios(2).banos(1).estado("publicada").imagenes(List.of("/img/prop-1-1.jpg")).build());
        usuarioRepository.save(Usuario.builder().run("333333333").nombre("Matías").apellidos("Fernández Lira")
                .correo("cliente@gmail.com").clave("x").rol(Rol.CLIENTE)
                .region("Región Metropolitana de Santiago").comuna("La Florida").direccion("Av. Vicuña Mackenna 8900").build());
    }

    @Test
    @DisplayName("Dado un cliente y una propiedad, cuando paga, entonces se guarda la boleta y baja el stock")
    void compraCompleta() throws Exception {
        String json = """
                {"usuarioCorreo":"cliente@gmail.com","propiedadIds":["PR-1001"],
                 "cliente":{"nombre":"Matías","apellidos":"Fernández Lira","correo":"cliente@gmail.com"},
                 "direccion":{"calle":"Av. Vicuña Mackenna 8900","region":"Región Metropolitana de Santiago","comuna":"La Florida"},
                 "fechaInicio":"%s",
                 "tarjeta":{"titular":"Matías Fernández","numero":"4242 4242 4242 4242","vencimiento":"12/39","cvv":"123"}}
                """.formatted(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/v1/boletas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("pagada"))
                .andExpect(jsonPath("$.total").value(600000))
                .andExpect(jsonPath("$.items[0].propiedadId").value("PR-1001"));

        assertThat(productoRepository.findById("PR-1001").orElseThrow().getUnidadesDisponibles()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/boletas/usuario/cliente@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Dada una categoría con propiedades, cuando se elimina por la API, entonces responde 409")
    void categoriaConProductos() throws Exception {
        mockMvc.perform(delete("/api/v1/categorias/departamento"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("La versión v2 entrega enlaces HATEOAS")
    void hateoas() throws Exception {
        mockMvc.perform(get("/api/v2/productos/PR-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categoria.href").exists());
    }

    @Test
    @DisplayName("Swagger publica la documentación de la API")
    void swagger() throws Exception {
        mockMvc.perform(get("/doc/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API 2026 AuraArriendos"));
    }
}
