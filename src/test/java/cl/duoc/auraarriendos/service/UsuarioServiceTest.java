package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.UsuarioDTO;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.BoletaRepository;
import cl.duoc.auraarriendos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService: registro y edición de usuarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BoletaRepository boletaRepository;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, boletaRepository);
    }

    @Test
    @DisplayName("Dado un usuario válido, cuando se crea, entonces la contraseña se guarda encriptada")
    void crearEncriptaClave() {
        // Given
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        UsuarioDTO dto = usuarioService.crear(Datos.usuarioRequest("123456785", "camila@gmail.com", "clave123"));
        // Then
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getClave()).isNotEqualTo("clave123").startsWith("$2a$");
        assertThat(usuarioService.claveCorrecta(captor.getValue(), "clave123")).isTrue();
        assertThat(dto.tipoUsuario()).isEqualTo("Cliente");
    }

    @Test
    @DisplayName("Dado un RUN con DV incorrecto, cuando se crea, entonces lanza error")
    void runInvalido() {
        assertThatThrownBy(() -> usuarioService.crear(Datos.usuarioRequest("123456789", "camila@gmail.com", "clave123")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("RUN");
    }

    @Test
    @DisplayName("Dado un correo de dominio no permitido, cuando se crea, entonces lanza error")
    void dominioNoPermitido() {
        assertThatThrownBy(() -> usuarioService.crear(Datos.usuarioRequest("123456785", "camila@hotmail.com", "clave123")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("correos");
    }

    @Test
    @DisplayName("Dado un correo ya registrado, cuando se crea, entonces lanza 409")
    void correoDuplicado() {
        when(usuarioRepository.existsByCorreoIgnoreCase("cliente@gmail.com")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.crear(Datos.usuarioRequest("123456785", "cliente@gmail.com", "clave123")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("Dado el registro público, cuando se pide rol CLIENTE, entonces se ignora el tipo enviado")
    void registroSiempreCliente() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        UsuarioDTO dto = usuarioService.crear(Datos.usuarioRequest("123456785", "camila@gmail.com", "clave123"), Rol.CLIENTE);
        assertThat(dto.tipoUsuario()).isEqualTo("Cliente");
    }

    @Test
    @DisplayName("Dada una edición con contraseña en blanco, cuando se guarda, entonces se conserva la anterior")
    void editarConservaClave() {
        Usuario existente = Datos.cliente();
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.actualizar(3L, Datos.usuarioRequest("333333333", "cliente@gmail.com", ""));

        assertThat(existente.getClave()).isEqualTo("$2a$10$hash");
        assertThat(existente.getRun()).isEqualTo("333333333");
    }

    @Test
    @DisplayName("Dado un usuario con boletas, cuando se elimina, entonces lanza 409")
    void eliminarConBoletas() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(Datos.cliente()));
        when(boletaRepository.existsByUsuarioId(3L)).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.eliminar(3L)).isInstanceOf(ReglaNegocioException.class);
        verify(usuarioRepository, never()).delete(any());
    }
}
