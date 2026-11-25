package cl.duoc.auraarriendos.controller;

import cl.duoc.auraarriendos.controller.v1.ProductoController;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.service.Datos;
import cl.duoc.auraarriendos.service.Mapper;
import cl.duoc.auraarriendos.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false) // aquí se prueba el controlador; la seguridad se prueba en SeguridadIntegrationTest
@DisplayName("ProductoController v1: respuestas HTTP")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProductoService productoService;

    @Test
    @DisplayName("GET /api/v1/productos responde 200 con el catálogo")
    void listar() throws Exception {
        when(productoService.listarPublicadas())
                .thenReturn(List.of(Mapper.aDto(Datos.producto("PR-1001", 650000, null, 3))));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("PR-1001"))
                .andExpect(jsonPath("$[0].categoria").value("Departamento"));
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} inexistente responde 404 con mensaje")
    void noEncontrado() throws Exception {
        when(productoService.obtener("PR-9999")).thenThrow(new RecursoNoEncontradoException("No existe la propiedad PR-9999"));

        mockMvc.perform(get("/api/v1/productos/PR-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la propiedad PR-9999"));
    }

    @Test
    @DisplayName("POST /api/v1/productos sin nombre responde 400 con el error del campo")
    void crearInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Casa\",\"precio\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/v1/productos válido responde 201 con Location")
    void crear() throws Exception {
        when(productoService.crear(any())).thenReturn(Mapper.aDto(Datos.producto("PR-1011", 890000, null, 2)));
        String json = """
                {"nombre":"Casa con patio","categoria":"Departamento","region":"Región Metropolitana de Santiago",
                 "comuna":"La Reina","direccion":"Av. Larraín 6100","m2":95,"precio":890000,
                 "unidadesDisponibles":2,"alertaDisponibilidad":1,"dormitorios":3,"banos":2}
                """;
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/productos/PR-1011")));
    }
}
