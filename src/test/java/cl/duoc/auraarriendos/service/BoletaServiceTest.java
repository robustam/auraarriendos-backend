package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.BoletaDTO;
import cl.duoc.auraarriendos.dto.ResultadoPago;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Boleta;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.repository.BoletaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoletaService: checkout, pago y stock")
class BoletaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 17);

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private PagoService pagoService;
    private BoletaService boletaService;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZoneId.of("America/Santiago")).toInstant(), ZoneId.of("America/Santiago"));
        boletaService = new BoletaService(boletaRepository, productoRepository, usuarioService, pagoService, reloj);
    }

    @Test
    @DisplayName("Dado un pago aprobado, cuando se crea la boleta, entonces queda pagada, usa el precio oferta y descuenta stock")
    void pagoAprobado() {
        // Given
        Producto p = Datos.producto("PR-1004", 850000, 790000, 2);
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1004")).thenReturn(Optional.of(p));
        when(pagoService.procesar(anyString(), eq(790000))).thenReturn(ResultadoPago.aprobado("AUT-123456"));
        when(boletaRepository.findUltimoNumero()).thenReturn(20260003L);
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        BoletaDTO boleta = boletaService.crear(
                Datos.boletaRequest(List.of("PR-1004"), "4242 4242 4242 4242", HOY.plusDays(14)), "cliente@gmail.com");
        // Then
        assertThat(boleta.estado()).isEqualTo("pagada");
        assertThat(boleta.numero()).isEqualTo(20260004L);
        assertThat(boleta.total()).isEqualTo(790000);
        assertThat(boleta.tarjeta()).isEqualTo("•••• 4242");
        assertThat(boleta.items()).hasSize(1);
        assertThat(p.getUnidadesDisponibles()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dado un pago rechazado, cuando se crea la boleta, entonces queda rechazada y el stock no cambia")
    void pagoRechazado() {
        Producto p = Datos.producto("PR-1002", 980000, null, 1);
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1002")).thenReturn(Optional.of(p));
        when(pagoService.procesar(anyString(), anyInt()))
                .thenReturn(ResultadoPago.rechazado("Tarjeta rechazada por el banco emisor."));
        when(boletaRepository.findUltimoNumero()).thenReturn(20260000L);
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaDTO boleta = boletaService.crear(
                Datos.boletaRequest(List.of("PR-1002"), "4000 0000 0000 0002", HOY), "cliente@gmail.com");

        assertThat(boleta.estado()).isEqualTo("rechazada");
        assertThat(boleta.motivoRechazo()).contains("rechazada");
        assertThat(p.getUnidadesDisponibles()).isEqualTo(1);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dada una propiedad sin stock, cuando se reserva, entonces lanza error y no se cobra")
    void sinStock() {
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 0)));

        assertThatThrownBy(() -> boletaService.crear(
                Datos.boletaRequest(List.of("PR-1001"), "4242424242424242", HOY), "cliente@gmail.com"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("disponible");
        verifyNoInteractions(pagoService);
    }

    @Test
    @DisplayName("Dada una fecha de inicio en el pasado, cuando se reserva, entonces lanza error")
    void fechaPasada() {
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));

        assertThatThrownBy(() -> boletaService.crear(
                Datos.boletaRequest(List.of("PR-1001"), "4242424242424242", HOY.minusDays(1)), "cliente@gmail.com"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("fecha");
    }

    @Test
    @DisplayName("Dada una tarjeta que no pasa Luhn, cuando se reserva, entonces lanza error sin llamar a la pasarela")
    void tarjetaInvalida() {
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(Datos.producto("PR-1001", 650000, null, 3)));

        assertThatThrownBy(() -> boletaService.crear(
                Datos.boletaRequest(List.of("PR-1001"), "4242 4242 4242 4241", HOY), "cliente@gmail.com"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("tarjeta");
        verifyNoInteractions(pagoService);
    }

    @Test
    @DisplayName("Dadas propiedades repetidas en el carrito, cuando se reserva, entonces se cobra una sola vez cada una")
    void sinDuplicados() {
        Producto p = Datos.producto("PR-1001", 650000, null, 3);
        when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(Datos.cliente());
        when(productoRepository.findById("PR-1001")).thenReturn(Optional.of(p));
        when(pagoService.procesar(anyString(), eq(650000))).thenReturn(ResultadoPago.aprobado("AUT-000001"));
        when(boletaRepository.findUltimoNumero()).thenReturn(20260000L);
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaDTO boleta = boletaService.crear(
                Datos.boletaRequest(List.of("PR-1001", "PR-1001"), "4242424242424242", HOY), "cliente@gmail.com");

        assertThat(boleta.total()).isEqualTo(650000);
        assertThat(boleta.items()).hasSize(1);
    }
}
