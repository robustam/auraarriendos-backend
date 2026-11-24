package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso a la tabla BOLETA. */
@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    /** Todas las boletas, de la más reciente a la más antigua. */
    List<Boleta> findAllByOrderByFechaDesc();

    /** Historial de compras de un usuario. */
    List<Boleta> findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(String correo);

    /** Boletas por estado: "pagada" o "rechazada". */
    List<Boleta> findByEstadoOrderByFechaDesc(String estado);

    boolean existsByUsuarioId(Long usuarioId);

    /** Último número usado, para generar el correlativo de la siguiente boleta. */
    @Query("SELECT COALESCE(MAX(b.numero), 20260000) FROM Boleta b")
    Long findUltimoNumero();
}
