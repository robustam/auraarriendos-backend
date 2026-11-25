package cl.duoc.auraarriendos.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService: generación y verificación de tokens")
class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-auraarriendos-2026-solo-tests-unitarios";
    private final JwtService jwtService = new JwtService(SECRETO, 15, 7);

    @Test
    @DisplayName("Dado un usuario, cuando se genera el token de acceso, entonces contiene su correo y rol")
    void generarYValidar() {
        String token = jwtService.generarAcceso("admin@duoc.cl", "ADMIN");
        Optional<Claims> claims = jwtService.validar(token, JwtService.TIPO_ACCESO);
        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("admin@duoc.cl");
        assertThat(claims.get().get("rol", String.class)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Un refresh token no sirve como token de acceso")
    void tipoIncorrecto() {
        String refresh = jwtService.generarRefresh("cliente@gmail.com", "id-1");
        assertThat(jwtService.validar(refresh, JwtService.TIPO_ACCESO)).isEmpty();
        assertThat(jwtService.validar(refresh, JwtService.TIPO_REFRESH)).isPresent();
    }

    @Test
    @DisplayName("Un token alterado o firmado con otra clave es rechazado")
    void tokenAlterado() {
        String token = jwtService.generarAcceso("cliente@gmail.com", "CLIENTE");
        JwtService otro = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres!!", 15, 7);
        assertThat(otro.validar(token, JwtService.TIPO_ACCESO)).isEmpty();
        // Cambiar el contenido (por ejemplo, el rol) invalida la firma
        String[] partes = token.split("\\.");
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"cliente@gmail.com\",\"rol\":\"ADMIN\",\"tipo\":\"access\"}".getBytes());
        assertThat(jwtService.validar(partes[0] + "." + payloadFalso + "." + partes[2], JwtService.TIPO_ACCESO)).isEmpty();
        assertThat(jwtService.validar("basura", JwtService.TIPO_ACCESO)).isEmpty();
    }

    @Test
    @DisplayName("Un token vencido es rechazado")
    void tokenVencido() {
        JwtService corto = new JwtService(SECRETO, 0, 7);
        String token = corto.generarAcceso("cliente@gmail.com", "CLIENTE");
        assertThat(corto.validar(token, JwtService.TIPO_ACCESO)).isEmpty();
    }

    @Test
    @DisplayName("Un secreto corto no se acepta")
    void secretoCorto() {
        assertThatThrownBy(() -> new JwtService("corto", 15, 7)).isInstanceOf(IllegalStateException.class);
    }
}
