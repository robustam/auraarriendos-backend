package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.CategoriaDTO;
import cl.duoc.auraarriendos.dto.CategoriaRequest;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Categoria;
import cl.duoc.auraarriendos.repository.CategoriaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService: reglas de las categorías")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    @DisplayName("Dado un nombre nuevo, cuando se crea, entonces el id es el nombre sin tildes ni espacios")
    void crearGeneraSlug() {
        // Given
        when(categoriaRepository.existsByNombreIgnoreCase("Casa de Campo")).thenReturn(false);
        when(categoriaRepository.existsById("casa-de-campo")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        CategoriaDTO dto = categoriaService.crear(new CategoriaRequest("Casa de Campo", "Parcelas", null));
        // Then
        assertThat(dto.id()).isEqualTo("casa-de-campo");
        assertThat(dto.nombre()).isEqualTo("Casa de Campo");
    }

    @Test
    @DisplayName("Dado un nombre repetido, cuando se crea, entonces lanza 409")
    void crearDuplicada() {
        when(categoriaRepository.existsByNombreIgnoreCase("Casa")).thenReturn(true);
        assertThatThrownBy(() -> categoriaService.crear(new CategoriaRequest("Casa", null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    @DisplayName("Dada una categoría con propiedades, cuando se elimina, entonces lanza 409")
    void eliminarConProductos() {
        when(categoriaRepository.findById("departamento")).thenReturn(Optional.of(Datos.departamento()));
        when(productoRepository.existsByCategoriaId("departamento")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.eliminar("departamento"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("propiedades");
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Dado el nombre de una categoría, cuando se busca por id o nombre, entonces la encuentra")
    void buscarPorNombre() {
        when(categoriaRepository.findById("Departamento")).thenReturn(Optional.empty());
        when(categoriaRepository.findByNombreIgnoreCase("Departamento")).thenReturn(Optional.of(Datos.departamento()));

        assertThat(categoriaService.buscarPorIdONombre("Departamento").getId()).isEqualTo("departamento");
    }
}
