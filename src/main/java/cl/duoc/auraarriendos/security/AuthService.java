package cl.duoc.auraarriendos.security;

import cl.duoc.auraarriendos.dto.AuthResponse;
import cl.duoc.auraarriendos.dto.UsuarioDTO;
import cl.duoc.auraarriendos.dto.UsuarioRequest;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.RefreshToken;
import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.RefreshTokenRepository;
import cl.duoc.auraarriendos.service.Mapper;
import cl.duoc.auraarriendos.service.UsuarioService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Inicio de sesión, registro, renovación (con rotación) y cierre de sesión. */
@Service
@Transactional
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos";

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UsuarioService usuarioService, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponse login(String correo, String clave) {
        Usuario usuario;
        try {
            usuario = usuarioService.buscarPorCorreo(correo);
        } catch (RecursoNoEncontradoException e) {
            // Mismo mensaje en ambos casos: no se revela si el correo existe
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        if (!usuarioService.claveCorrecta(usuario, clave)) {
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        return emitirTokens(usuario);
    }

    /** Registro público: siempre crea un CLIENTE, aunque se envíe otro tipo de usuario. */
    public AuthResponse registrar(UsuarioRequest req) {
        UsuarioDTO creado = usuarioService.crear(req, Rol.CLIENTE);
        return emitirTokens(usuarioService.buscarPorCorreo(creado.correo()));
    }

    /**
     * Renovación con ROTACIÓN: el refresh token usado se revoca y se entrega uno nuevo.
     * Si alguien intenta usar un refresh token ya revocado (posible robo), se cierran todas las sesiones del usuario.
     * noRollbackFor: la revocación masiva debe quedar guardada aunque luego se responda con error 401.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public AuthResponse renovar(String refreshToken) {
        Claims claims = jwtService.validar(refreshToken, JwtService.TIPO_REFRESH)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "La sesión expiró, inicia sesión nuevamente"));
        RefreshToken guardado = refreshTokenRepository.findById(claims.getId())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "La sesión no es válida"));
        if (guardado.isRevocado()) {
            refreshTokenRepository.revocarTodos(guardado.getCorreo());
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "La sesión ya fue renovada o cerrada, inicia sesión nuevamente");
        }
        if (guardado.getExpira().isBefore(Instant.now())) {
            throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "La sesión expiró, inicia sesión nuevamente");
        }
        guardado.setRevocado(true);
        refreshTokenRepository.save(guardado);
        return emitirTokens(usuarioService.buscarPorCorreo(guardado.getCorreo()));
    }

    /** Cierra la sesión revocando el refresh token (el de acceso vence solo en pocos minutos). */
    public void cerrarSesion(String refreshToken) {
        jwtService.validar(refreshToken, JwtService.TIPO_REFRESH)
                .flatMap(c -> refreshTokenRepository.findById(c.getId()))
                .ifPresent(t -> {
                    t.setRevocado(true);
                    refreshTokenRepository.save(t);
                });
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        String jti = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .id(jti)
                .correo(usuario.getCorreo())
                .expira(jwtService.expiracionRefresh())
                .revocado(false)
                .build());
        return new AuthResponse(
                jwtService.generarAcceso(usuario.getCorreo(), usuario.getRol().name()),
                jwtService.generarRefresh(usuario.getCorreo(), jti),
                "Bearer",
                jwtService.segundosAcceso(),
                Mapper.aDto(usuario));
    }
}
