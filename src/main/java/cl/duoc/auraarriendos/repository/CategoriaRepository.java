package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Acceso a la tabla CATEGORIA. Los métodos se generan solos a partir de su nombre (ORM). */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
