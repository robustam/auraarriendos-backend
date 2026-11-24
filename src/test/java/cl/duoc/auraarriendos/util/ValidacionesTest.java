package cl.duoc.auraarriendos.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validaciones de negocio (RUN, correo, tarjeta)")
class ValidacionesTest {

    @ParameterizedTest
    @ValueSource(strings = {"111111111", "222222222", "123456785", "10000013K", "10000013k", "100000040"})
    @DisplayName("Dado un RUN con dígito verificador correcto, cuando se valida, entonces es válido")
    void runValido(String run) {
        // Given: un RUN bien escrito / When: se valida / Then: es aceptado
        assertThat(Validaciones.runValido(run)).isTrue();
    }

    @Test
    @DisplayName("Dado un RUN con dígito verificador incorrecto, cuando se valida, entonces es inválido")
    void runInvalido() {
        assertThat(Validaciones.runValido("111111112")).isFalse();
        assertThat(Validaciones.runValido("11.111.111-1")).isFalse();
        assertThat(Validaciones.runValido(null)).isFalse();
    }

    @Test
    @DisplayName("Dado un cuerpo de RUN, cuando se calcula el DV, entonces usa módulo 11")
    void calcularDv() {
        assertThat(Validaciones.calcularDv("11111111")).isEqualTo('1');
        assertThat(Validaciones.calcularDv("12345678")).isEqualTo('5');
    }

    @Test
    @DisplayName("Solo se aceptan correos duoc.cl, profesor.duoc.cl y gmail.com")
    void correoPermitido() {
        assertThat(Validaciones.correoPermitido("ana@duoc.cl")).isTrue();
        assertThat(Validaciones.correoPermitido("profe@profesor.duoc.cl")).isTrue();
        assertThat(Validaciones.correoPermitido("ana@gmail.com")).isTrue();
        assertThat(Validaciones.correoPermitido("ana@hotmail.com")).isFalse();
        assertThat(Validaciones.correoPermitido("sin-arroba")).isFalse();
    }

    @Test
    @DisplayName("Dado un número de tarjeta, cuando se valida con Luhn, entonces detecta errores de tipeo")
    void tarjetaLuhn() {
        assertThat(Validaciones.tarjetaValida("4242 4242 4242 4242")).isTrue();
        assertThat(Validaciones.tarjetaValida("4000000000000002")).isTrue();
        assertThat(Validaciones.tarjetaValida("4242 4242 4242 4241")).isFalse();
        assertThat(Validaciones.tarjetaValida("1234")).isFalse();
    }

    @Test
    @DisplayName("El vencimiento debe tener formato MM/AA y no estar en el pasado")
    void vencimiento() {
        YearMonth hoy = YearMonth.of(2026, 11);
        assertThat(Validaciones.vencimientoValido("11/26", hoy)).isTrue();
        assertThat(Validaciones.vencimientoValido("12/28", hoy)).isTrue();
        assertThat(Validaciones.vencimientoValido("10/26", hoy)).isFalse();
        assertThat(Validaciones.vencimientoValido("13/28", hoy)).isFalse();
    }

    @Test
    @DisplayName("Un nombre se convierte en un id legible sin tildes")
    void slug() {
        assertThat(Validaciones.slug("Casa de Campo")).isEqualTo("casa-de-campo");
        assertThat(Validaciones.slug("Oficina Ñuñoa")).isEqualTo("oficina-nunoa");
    }
}
