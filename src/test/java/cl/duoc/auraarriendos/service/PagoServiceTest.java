package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.ResultadoPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PagoService: pasarela de pago simulada")
class PagoServiceTest {

    private final PagoService pagoService = new PagoService();

    @Test
    @DisplayName("Dada la tarjeta de prueba 4242, cuando se paga, entonces se aprueba con código AUT-")
    void aprobado() {
        ResultadoPago r = pagoService.procesar("4242 4242 4242 4242", 650000);
        assertThat(r.aprobado()).isTrue();
        assertThat(r.codigoAutorizacion()).matches("AUT-\\d{6}");
    }

    @Test
    @DisplayName("Dada la tarjeta 4000 0000 0000 0002, cuando se paga, entonces el banco la rechaza")
    void rechazado() {
        ResultadoPago r = pagoService.procesar("4000 0000 0000 0002", 650000);
        assertThat(r.aprobado()).isFalse();
        assertThat(r.motivo()).contains("rechazada");
    }

    @Test
    @DisplayName("Dado un monto 0, cuando se paga, entonces se rechaza")
    void montoInvalido() {
        assertThat(pagoService.procesar("4242424242424242", 0).aprobado()).isFalse();
    }
}
