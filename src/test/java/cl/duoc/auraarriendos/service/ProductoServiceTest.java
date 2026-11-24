package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.ProductoDTO;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.repository.DetalleBoletaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService: reglas de negocio de las propiedades")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;
    @Mock
    private CategoriaService categoriaService;
    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Dado el catálogo, cuando se listan las publicadas, entonces solo se piden las de estado publicada")
    void listarPublicadas() {
        // Given
        when(productoRepository.findByEstadoOrderByIdAsc("publicada"))
                .thenReturn(List.of(Datos.producto("PR-1001", 650000, null, 3)));
        // When
        List<ProductoDTO> resultado = productoService.listarPublicadas();
        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo("Departamento");
        assertThat(resultado.get(0).imagen()).isEqualTo("/img/prop-1-1.jpg");
    }

    @Test
    @DisplayName("Dado un id inexistente, cuando se obtiene, entonces lanza 404")
    void obtenerInexistente() {
        when(productoRepository.findById("PR-9999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productoService.obtener("PR-9999"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("PR-9999");
    }

    @Test
    @DisplayName("Dado un producto válido, cuando se crea, entonces recibe el siguiente id correlativo y queda publicado")
    void crearAsignaId() {
        // Given
        when(productoRepository.findAllIds()).thenReturn(List.of("PR-1001", "PR-1010", "PR-2001"));
        when(productoRepository.existsByCodigoIgnoreCase("PR-2002")).thenReturn(false);
        when(categoriaService.buscarPorIdONombre("Departamento")).thenReturn(Datos.departamento());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        ProductoDTO creado = productoService.crear(Datos.productoRequest(890000, null));
        // Then
        assertThat(creado.id()).isEqualTo("PR-2002");
        assertThat(creado.estado()).isEqualTo("publicada");
        assertThat(creado.categoriaId()).isEqualTo("departamento");
    }

    @Test
    @DisplayName("Dada una oferta mayor o igual al precio, cuando se crea, entonces se rechaza con 400")
    void ofertaInvalida() {
        assertThatThrownBy(() -> productoService.crear(Datos.productoRequest(500000, 500000)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("oferta");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado un cliente que publica, cuando se guarda, entonces queda pendiente y a su nombre")
    void publicarQuedaPendiente() {
        when(productoRepository.findAllIds()).thenReturn(List.of());
        when(categoriaService.buscarPorIdONombre("Departamento")).thenReturn(Datos.departamento());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO publicado = productoService.publicar(Datos.productoRequest(700000, null), "cliente@gmail.com");

        assertThat(publicado.id()).isEqualTo("PR-1001");
        assertThat(publicado.estado()).isEqualTo("pendiente");
        assertThat(publicado.publicadoPor()).isEqualTo("cliente@gmail.com");
    }

    @Test
    @DisplayName("Dado un rechazo sin motivo, cuando se cambia el estado, entonces lanza error")
    void rechazoSinMotivo() {
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));
        assertThatThrownBy(() -> productoService.cambiarEstado("PR-1001", "rechazada", " "))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Dado un rechazo con motivo, cuando se cambia el estado, entonces se guarda el motivo")
    void rechazoConMotivo() {
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO dto = productoService.cambiarEstado("PR-1001", "rechazada", "Fotos borrosas");

        assertThat(dto.estado()).isEqualTo("rechazada");
        assertThat(dto.motivoRechazo()).isEqualTo("Fotos borrosas");
    }

    @Test
    @DisplayName("Dada una propiedad con boletas, cuando se elimina, entonces lanza 409 y no se borra")
    void eliminarConBoletas() {
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));
        when(detalleBoletaRepository.existsByProductoId("PR-1001")).thenReturn(true);

        assertThatThrownBy(() -> productoService.eliminar("PR-1001"))
                .isInstanceOf(ReglaNegocioException.class)
                .satisfies(e -> assertThat(((ReglaNegocioException) e).getEstado()).isEqualTo(HttpStatus.CONFLICT));
        verify(productoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Dada una propiedad, cuando se marca arrendada, entonces guarda la fecha")
    void marcarArrendada() {
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO dto = productoService.marcarArrendada("PR-1001", true);

        assertThat(dto.arrendada()).isTrue();
        assertThat(dto.fechaArrendada()).isNotNull();
    }
}
