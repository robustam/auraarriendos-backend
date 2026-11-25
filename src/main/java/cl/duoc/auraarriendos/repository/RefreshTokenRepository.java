package cl.duoc.auraarriendos.repository;

import cl.duoc.auraarriendos.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /** Cierra todas las sesiones de un usuario (se usa si se detecta reutilización de un token). */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE LOWER(r.correo) = LOWER(?1)")
    int revocarTodos(String correo);
}
