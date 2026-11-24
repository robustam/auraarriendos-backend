package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.BoletaDTO;
import cl.duoc.auraarriendos.dto.BoletaRequest;
import cl.duoc.auraarriendos.dto.ResultadoPago;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Boleta;
import cl.duoc.auraarriendos.model.DetalleBoleta;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.model.Usuario;
import cl.duoc.auraarriendos.repository.BoletaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import cl.duoc.auraarriendos.util.Validaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Lógica de negocio del checkout y de las boletas.
 * El flujo de una compra es:
 *  1. Validar que las propiedades existan y estén disponibles.
 *  2. Validar la fecha de inicio y la tarjeta.
 *  3. Calcular el total con los precios de la base de datos (oferta incluida) y cobrar.
 *  4. Guardar la BOLETA con su DETALLE_BOLETA.
 *  5. Si quedó pagada, descontar 1 unidad de stock de cada propiedad.
 * Todo ocurre en una transacción: si algo falla, no se guarda nada a medias.
 */
@Service
@Transactional
public class BoletaService {

    public static final String PAGADA = "pagada";
    public static final String RECHAZADA = "rechazada";

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final PagoService pagoService;
    private final Clock clock;

    @Autowired
    public BoletaService(BoletaRepository boletaRepository, ProductoRepository productoRepository,
                         UsuarioService usuarioService, PagoService pagoService) {
        this(boletaRepository, productoRepository, usuarioService, pagoService, Clock.systemDefaultZone());
    }

    /** Constructor con reloj, para poder fijar la fecha en las pruebas. */
    BoletaService(BoletaRepository boletaRepository, ProductoRepository productoRepository,
                  UsuarioService usuarioService, PagoService pagoService, Clock clock) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.pagoService = pagoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<BoletaDTO> listar(String estado) {
        List<Boleta> boletas = (estado == null || estado.isBlank())
                ? boletaRepository.findAllByOrderByFechaDesc()
                : boletaRepository.findByEstadoOrderByFechaDesc(estado.trim().toLowerCase());
        return boletas.stream().map(Mapper::aDto).toList();
    }

    @Transactional(readOnly = true)
    public BoletaDTO obtener(Long id) {
        return Mapper.aDto(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<BoletaDTO> listarPorUsuario(String correo) {
        return boletaRepository.findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(correo).stream()
                .map(Mapper::aDto).toList();
    }

    /** Procesa el pago de la reserva y genera la boleta (pagada o rechazada). */
    public BoletaDTO crear(BoletaRequest req, String correoUsuario) {
        Usuario usuario = usuarioService.buscarPorCorreo(correoUsuario);

        // 1. Propiedades sin repetir, existentes y disponibles
        List<Producto> productos = new ArrayList<>();
        for (String id : new LinkedHashSet<>(req.propiedadIds())) {
            Producto p = productoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe la propiedad " + id));
            if (!p.disponibleParaReservar()) {
                throw new ReglaNegocioException("La propiedad " + p.getNombre() + " ya no está disponible para reservar");
            }
            productos.add(p);
        }

        // 2. Fecha de inicio desde hoy y tarjeta válida (antes de llamar a la pasarela)
        if (req.fechaInicio().isBefore(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de inicio del arriendo debe ser desde hoy en adelante");
        }
        BoletaRequest.Tarjeta tarjeta = req.tarjeta();
        if (!Validaciones.tarjetaValida(tarjeta.numero())) {
            throw new ReglaNegocioException("El número de tarjeta no es válido");
        }
        if (!Validaciones.vencimientoValido(tarjeta.vencimiento(), YearMonth.now(clock))) {
            throw new ReglaNegocioException("La tarjeta está vencida o la fecha no tiene el formato MM/AA");
        }

        // 3. Total calculado en el servidor (el frontend no puede cambiar los precios) y cobro
        int total = productos.stream().mapToInt(Producto::precioFinal).sum();
        ResultadoPago pago = pagoService.procesar(tarjeta.numero(), total);

        // 4. Boleta con sus detalles
        String digitos = tarjeta.numero().replaceAll("\\D", "");
        Boleta boleta = Boleta.builder()
                .numero(boletaRepository.findUltimoNumero() + 1)
                .fecha(LocalDateTime.now(clock))
                .estado(pago.aprobado() ? PAGADA : RECHAZADA)
                .usuario(usuario)
                .clienteNombre(req.cliente().nombre().trim())
                .clienteApellidos(req.cliente().apellidos().trim())
                .clienteCorreo(req.cliente().correo().trim().toLowerCase())
                .calle(req.direccion().calle().trim())
                .departamento(req.direccion().departamento())
                .region(req.direccion().region())
                .comuna(req.direccion().comuna())
                .indicaciones(req.direccion().indicaciones())
                .fechaInicio(req.fechaInicio())
                .tarjeta("•••• " + digitos.substring(digitos.length() - 4))
                .total(total)
                .codigoAutorizacion(pago.codigoAutorizacion())
                .motivoRechazo(pago.motivo())
                .build();

        for (Producto p : productos) {
            boleta.agregarDetalle(DetalleBoleta.builder()
                    .producto(p)
                    .nombre(p.getNombre())
                    .comuna(p.getComuna())
                    .imagen(p.getImagenes() == null || p.getImagenes().isEmpty() ? null : p.getImagenes().get(0))
                    .precio(p.precioFinal())
                    .build());
            // 5. Una reserva pagada descuenta stock; una rechazada no cambia nada
            if (pago.aprobado()) {
                p.setUnidadesDisponibles(Math.max(0, p.getUnidadesDisponibles() - 1));
                productoRepository.save(p);
            }
        }
        return Mapper.aDto(boletaRepository.save(boleta));
    }

    /** Elimina una boleta (por ejemplo, registros de prueba). */
    public void eliminar(Long id) {
        boletaRepository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la boleta " + id));
    }
}
