package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.ProductoDTO;
import cl.duoc.auraarriendos.dto.ProductoRequest;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.repository.DetalleBoletaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Lógica de negocio de los productos (propiedades en arriendo). */
@Service
@Transactional
public class ProductoService {

    public static final String PUBLICADA = "publicada";
    public static final String PENDIENTE = "pendiente";
    public static final String RECHAZADA = "rechazada";
    private static final Set<String> ESTADOS = Set.of(PUBLICADA, PENDIENTE, RECHAZADA);

    private final ProductoRepository productoRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository,
                           DetalleBoletaRepository detalleBoletaRepository,
                           CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
        this.categoriaService = categoriaService;
    }

    /** Catálogo público: solo las propiedades aprobadas. */
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPublicadas() {
        return productoRepository.findByEstadoOrderByIdAsc(PUBLICADA).stream().map(Mapper::aDto).toList();
    }

    /** Todas las propiedades (panel de administración). */
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodas() {
        return productoRepository.findAll().stream()
                .sorted(Comparator.comparing(Producto::getId))
                .map(Mapper::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtener(String id) {
        return Mapper.aDto(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(String categoriaId) {
        return productoRepository.findByCategoriaIdOrderByIdAsc(categoriaId).stream().map(Mapper::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorPublicador(String correo) {
        return productoRepository.findByPublicadoPorIgnoreCaseOrderByIdAsc(correo).stream().map(Mapper::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarStockCritico() {
        return productoRepository.findStockCritico().stream().map(Mapper::aDto).toList();
    }

    /** Crea una propiedad desde el panel (queda publicada salvo que se indique otro estado). */
    public ProductoDTO crear(ProductoRequest req) {
        validarReglas(req);
        Producto producto = new Producto();
        producto.setId(siguienteId());
        String codigo = vacio(req.codigo()) ? producto.getId() : req.codigo().trim();
        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El código " + codigo + " ya está en uso");
        }
        producto.setCodigo(codigo);
        producto.setEstado(vacio(req.estado()) ? PUBLICADA : validarEstado(req.estado()));
        producto.setPublicadoPor(req.publicadoPor());
        copiarDatos(req, producto);
        return Mapper.aDto(productoRepository.save(producto));
    }

    /** Un cliente publica su propiedad: queda "pendiente" hasta que el administrador la apruebe. */
    public ProductoDTO publicar(ProductoRequest req, String correoPublicador) {
        validarReglas(req);
        Producto producto = new Producto();
        producto.setId(siguienteId());
        producto.setCodigo(producto.getId());
        producto.setEstado(PENDIENTE);
        producto.setPublicadoPor(correoPublicador);
        copiarDatos(req, producto);
        producto.setDestacada(false);
        producto.setArrendada(false);
        producto.setFechaArrendada(null);
        return Mapper.aDto(productoRepository.save(producto));
    }

    public ProductoDTO actualizar(String id, ProductoRequest req) {
        validarReglas(req);
        Producto producto = buscar(id);
        if (!vacio(req.codigo()) && !req.codigo().trim().equalsIgnoreCase(producto.getCodigo())) {
            if (productoRepository.existsByCodigoIgnoreCase(req.codigo().trim())) {
                throw new ReglaNegocioException(HttpStatus.CONFLICT, "El código " + req.codigo() + " ya está en uso");
            }
            producto.setCodigo(req.codigo().trim());
        }
        if (!vacio(req.estado())) {
            producto.setEstado(validarEstado(req.estado()));
        }
        copiarDatos(req, producto);
        if (!RECHAZADA.equals(producto.getEstado())) {
            producto.setMotivoRechazo(null);
        }
        return Mapper.aDto(productoRepository.save(producto));
    }

    /** Moderación: aprobar (publicada) o rechazar (con motivo obligatorio). */
    public ProductoDTO cambiarEstado(String id, String estado, String motivo) {
        Producto producto = buscar(id);
        String nuevo = validarEstado(estado);
        if (RECHAZADA.equals(nuevo) && vacio(motivo)) {
            throw new ReglaNegocioException("Debe indicar el motivo del rechazo");
        }
        producto.setEstado(nuevo);
        producto.setMotivoRechazo(RECHAZADA.equals(nuevo) ? motivo.trim() : null);
        return Mapper.aDto(productoRepository.save(producto));
    }

    /** Marca la propiedad como arrendada (se oculta de la tienda) o la reactiva. */
    public ProductoDTO marcarArrendada(String id, boolean arrendada) {
        Producto producto = buscar(id);
        producto.setArrendada(arrendada);
        producto.setFechaArrendada(arrendada ? LocalDateTime.now() : null);
        return Mapper.aDto(productoRepository.save(producto));
    }

    /** Regla de negocio: no se elimina una propiedad que ya aparece en boletas (se perdería el historial). */
    public void eliminar(String id) {
        Producto producto = buscar(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "La propiedad " + id + " tiene boletas asociadas: márquela como arrendada en lugar de eliminarla");
        }
        productoRepository.delete(producto);
    }

    @Transactional(readOnly = true)
    public Producto buscar(String id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la propiedad " + id));
    }

    /** Siguiente correlativo PR-####, partiendo en PR-1001. */
    String siguienteId() {
        int max = 1000;
        for (String id : productoRepository.findAllIds()) {
            String numeros = id.replaceAll("\\D", "");
            if (!numeros.isEmpty()) {
                max = Math.max(max, Integer.parseInt(numeros));
            }
        }
        return "PR-" + (max + 1);
    }

    /** Reglas de negocio que no se pueden expresar con anotaciones. */
    private void validarReglas(ProductoRequest req) {
        if (req.precioOferta() != null && req.precioOferta() > 0 && req.precioOferta() >= req.precio()) {
            throw new ReglaNegocioException("El precio de oferta debe ser menor que el precio normal");
        }
    }

    private void copiarDatos(ProductoRequest req, Producto p) {
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion());
        p.setCategoria(categoriaService.buscarPorIdONombre(req.categoria()));
        p.setRegion(req.region());
        p.setComuna(req.comuna());
        p.setDireccion(req.direccion());
        p.setM2(req.m2());
        p.setPrecio(req.precio());
        p.setPrecioOferta(req.precioOferta() != null && req.precioOferta() > 0 ? req.precioOferta() : null);
        p.setUnidadesDisponibles(req.unidadesDisponibles());
        p.setAlertaDisponibilidad(req.alertaDisponibilidad());
        p.setDormitorios(req.dormitorios());
        p.setBanos(req.banos());
        p.setEstacionamiento(Boolean.TRUE.equals(req.estacionamiento()));
        p.setDestacada(Boolean.TRUE.equals(req.destacada()));
        if (req.arrendada() != null && !req.arrendada().equals(p.getArrendada())) {
            p.setArrendada(req.arrendada());
            p.setFechaArrendada(req.arrendada() ? LocalDateTime.now() : null);
        }
        if (p.getArrendada() == null) {
            p.setArrendada(false);
        }
        if (RECHAZADA.equals(p.getEstado()) && !vacio(req.motivoRechazo())) {
            p.setMotivoRechazo(req.motivoRechazo());
        }
        List<String> imagenes = new ArrayList<>();
        if (req.imagenes() != null) {
            req.imagenes().stream().filter(s -> !vacio(s)).forEach(imagenes::add);
        }
        if (imagenes.isEmpty() && !vacio(req.imagen())) {
            imagenes.add(req.imagen());
        }
        if (!imagenes.isEmpty()) {
            p.setImagenes(imagenes);
        } else if (p.getImagenes() == null || p.getImagenes().isEmpty()) {
            p.setImagenes(new ArrayList<>(List.of("/img/prop-1.jpg")));
        }
    }

    private String validarEstado(String estado) {
        String e = estado.trim().toLowerCase();
        if (!ESTADOS.contains(e)) {
            throw new ReglaNegocioException("Estado no válido: " + estado);
        }
        return e;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
