package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.Rol;
import cl.duoc.auraarriendos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla USUARIO. */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);

    List<Usuario> findByRol(Rol rol);
}
