package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso a la tabla DETALLE_BOLETA. */
@Repository
public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    /** Detalles en los que aparece un producto (para no borrar productos ya vendidos). */
    List<DetalleBoleta> findByProductoId(String productoId);

    boolean existsByProductoId(String productoId);
}
