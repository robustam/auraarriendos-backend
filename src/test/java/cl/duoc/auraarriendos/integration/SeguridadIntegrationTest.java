package cl.duoc.auraarriendos.integration;

import cl.duoc.auraarriendos.model.Categoria;
import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifica JWT (login, verificación, renovación con rotación) y las restricciones por rol. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración: seguridad JWT y roles")
class SeguridadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private BoletaRepository boletaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void preparar() {
        refreshTokenRepository.deleteAll();
        boletaRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
        categoriaRepository.save(Categoria.builder().id("casa").nombre("Casa").build());
        crear("111111111", "admin@duoc.cl", "admin123", Rol.ADMIN);
        crear("222222222", "vendedor@gmail.com", "vende123", Rol.VENDEDOR);
        crear("333333333", "cliente@gmail.com", "cliente123", Rol.CLIENTE);
    }

    private void crear(String run, String correo, String clave, Rol rol) {
        usuarioRepository.save(Usuario.builder().run(run).nombre("Nombre").apellidos("Apellido").correo(correo)
                .clave(new BCryptPasswordEncoder().encode(clave)).rol(rol)
                .region("Región Metropolitana de Santiago").comuna("Santiago").direccion("Calle 123").build());
    }

    private MvcResult login(String correo, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(correo, clave)))
                .andReturn();
    }

    private String acceso(String correo, String clave) throws Exception {
        return "Bearer " + JsonPath.read(login(correo, clave).getResponse().getContentAsString(), "$.accessToken");
    }

    @Test
    @DisplayName("Dadas credenciales correctas, cuando inicia sesión, entonces recibe access y refresh token")
    void loginCorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"clave\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.usuario.tipoUsuario").value("Administrador"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());
    }

    @Test
    @DisplayName("Dada una contraseña incorrecta, cuando inicia sesión, entonces responde 401")
    void loginIncorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    @DisplayName("El catálogo es público, pero /auth/me sin token responde 401")
    void publicoYPrivado() throws Exception {
        mockMvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Con un token válido, /auth/me devuelve el usuario de la sesión")
    void me() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", acceso("cliente@gmail.com", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("cliente@gmail.com"));
    }

    @Test
    @DisplayName("Un CLIENTE no puede listar usuarios ni crear categorías (403)")
    void clienteRestringido() throws Exception {
        String cliente = acceso("cliente@gmail.com", "cliente123");
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", cliente)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", cliente)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/categorias").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Oficina\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un CLIENTE no puede ver el historial de otro usuario (403)")
    void clienteNoVeOtros() throws Exception {
        String cliente = acceso("cliente@gmail.com", "cliente123");
        mockMvc.perform(get("/api/v1/boletas/usuario/cliente@gmail.com").header("Authorization", cliente))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas/usuario/admin@duoc.cl").header("Authorization", cliente))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un VENDEDOR puede ver productos y boletas, pero no modificar ni ver usuarios")
    void vendedorSoloLectura() throws Exception {
        String vendedor = acceso("vendedor@gmail.com", "vende123");
        mockMvc.perform(get("/api/v1/productos/todos").header("Authorization", vendedor)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", vendedor)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", vendedor)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/categorias/casa").header("Authorization", vendedor)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un ADMIN tiene acceso total")
    void adminTotal() throws Exception {
        String admin = acceso("admin@duoc.cl", "admin123");
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", admin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/categorias").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Oficina\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Dado un refresh token, cuando se renueva, entonces se entrega uno nuevo y el viejo deja de servir (rotación)")
    void renovacionConRotacion() throws Exception {
        String refresh1 = JsonPath.read(login("cliente@gmail.com", "cliente123").getResponse().getContentAsString(), "$.refreshToken");

        String respuesta = mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh1 + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        String refresh2 = JsonPath.read(respuesta, "$.refreshToken");

        // Reutilizar el refresh viejo: rechazado y se cierran todas las sesiones
        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh1 + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh2 + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Dado un logout, cuando se intenta renovar con ese refresh, entonces responde 401")
    void logout() throws Exception {
        String refresh = JsonPath.read(login("cliente@gmail.com", "cliente123").getResponse().getContentAsString(), "$.refreshToken");
        mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El registro público siempre crea un Cliente aunque pida ser Administrador")
    void registroSiempreCliente() throws Exception {
        String json = """
                {"run":"123456785","nombre":"Camila","apellidos":"Rojas","correo":"camila@gmail.com","clave":"clave123",
                 "tipoUsuario":"Administrador","region":"Región Metropolitana de Santiago","comuna":"Ñuñoa","direccion":"Irarrázaval 3000"}
                """;
        mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.tipoUsuario").value("Cliente"))
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("Swagger sigue siendo público")
    void swaggerPublico() throws Exception {
        mockMvc.perform(get("/doc/api-docs")).andExpect(status().isOk());
    }
}
