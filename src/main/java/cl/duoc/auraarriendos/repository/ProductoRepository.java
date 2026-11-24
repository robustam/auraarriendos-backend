package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso a la tabla PRODUCTO (propiedades en arriendo). */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    /** Productos por estado: "publicada", "pendiente" o "rechazada". */
    List<Producto> findByEstadoOrderByIdAsc(String estado);

    /** Productos de una categoría (por el id de la categoría). */
    List<Producto> findByCategoriaIdOrderByIdAsc(String categoriaId);

    /** Publicaciones de un usuario (Mis publicaciones). */
    List<Producto> findByPublicadoPorIgnoreCaseOrderByIdAsc(String correo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCategoriaId(String categoriaId);

    long countByCategoriaId(String categoriaId);

    /** Stock crítico: unidades disponibles en o bajo el nivel de alerta. */
    @Query("SELECT p FROM Producto p WHERE p.alertaDisponibilidad IS NOT NULL "
            + "AND p.unidadesDisponibles <= p.alertaDisponibilidad ORDER BY p.unidadesDisponibles ASC")
    List<Producto> findStockCritico();

    /** Todos los id, para calcular el siguiente correlativo PR-####. */
    @Query("SELECT p.id FROM Producto p")
    List<String> findAllIds();
}
