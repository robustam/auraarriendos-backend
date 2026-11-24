package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.UsuarioDTO;
import cl.duoc.auraarriendos.dto.UsuarioRequest;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.BoletaRepository;
import cl.duoc.auraarriendos.repository.UsuarioRepository;
import cl.duoc.auraarriendos.util.Validaciones;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Lógica de negocio de los usuarios. Las contraseñas se guardan encriptadas con BCrypt. */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BoletaRepository boletaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.boletaRepository = boletaRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNombre))
                .map(Mapper::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtener(Long id) {
        return Mapper.aDto(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorCorreo(String correo) {
        return Mapper.aDto(buscarPorCorreo(correo));
    }

    /** Crea un usuario. Si no se indica el tipo, queda como Cliente. */
    public UsuarioDTO crear(UsuarioRequest req) {
        return crear(req, Rol.desdeTexto(req.tipoUsuario()));
    }

    /** Crea un usuario con un rol fijo (el registro público siempre usa CLIENTE). */
    public UsuarioDTO crear(UsuarioRequest req, Rol rol) {
        validarReglas(req);
        String run = req.run().trim().toUpperCase();
        if (usuarioRepository.existsByRun(run)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El RUN ya está registrado");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(req.correo().trim())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (req.clave() == null || req.clave().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        Usuario usuario = Usuario.builder()
                .run(run)
                .correo(req.correo().trim().toLowerCase())
                .clave(passwordEncoder.encode(req.clave()))
                .rol(rol)
                .build();
        copiarDatos(req, usuario);
        return Mapper.aDto(usuarioRepository.save(usuario));
    }

    /** Edita un usuario. El RUN no cambia; la contraseña en blanco conserva la actual. */
    public UsuarioDTO actualizar(Long id, UsuarioRequest req) {
        validarReglas(req);
        Usuario usuario = buscar(id);
        String correo = req.correo().trim().toLowerCase();
        if (!correo.equalsIgnoreCase(usuario.getCorreo()) && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        usuario.setCorreo(correo);
        if (req.clave() != null && !req.clave().isBlank()) {
            usuario.setClave(passwordEncoder.encode(req.clave()));
        }
        if (req.tipoUsuario() != null && !req.tipoUsuario().isBlank()) {
            usuario.setRol(Rol.desdeTexto(req.tipoUsuario()));
        }
        copiarDatos(req, usuario);
        return Mapper.aDto(usuarioRepository.save(usuario));
    }

    /** Regla de negocio: un usuario con boletas no se elimina (es su historial de compras). */
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (boletaRepository.existsByUsuarioId(id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El usuario " + usuario.getCorreo() + " tiene boletas y no se puede eliminar");
        }
        usuarioRepository.delete(usuario);
    }

    /** Compara una contraseña escrita con la guardada (encriptada). */
    public boolean claveCorrecta(Usuario usuario, String claveEscrita) {
        return claveEscrita != null && passwordEncoder.matches(claveEscrita, usuario.getClave());
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo == null ? "" : correo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + correo));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }

    private void validarReglas(UsuarioRequest req) {
        if (!Validaciones.runValido(req.run())) {
            throw new ReglaNegocioException("El RUN no es válido (revisa el dígito verificador)");
        }
        if (!Validaciones.correoPermitido(req.correo())) {
            throw new ReglaNegocioException("Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com");
        }
    }

    private void copiarDatos(UsuarioRequest req, Usuario u) {
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos().trim());
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setRegion(req.region());
        u.setComuna(req.comuna());
        u.setDireccion(req.direccion().trim());
    }
}
