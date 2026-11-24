package cl.duoc.auraarriendos.config;

import cl.duoc.auraarriendos.model.*;
import cl.duoc.auraarriendos.repository.BoletaRepository;
import cl.duoc.auraarriendos.repository.CategoriaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import cl.duoc.auraarriendos.repository.UsuarioRepository;
import cl.duoc.auraarriendos.util.Validaciones;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Carga datos iniciales cuando la base de datos está vacía (perfiles dev y docker).
 *  1. Los datos "oficiales" del sitio (categorías, propiedades, usuarios y boletas) desde /data/*.json,
 *     que son los mismos que usaba el frontend.
 *  2. Datos de ejemplo generados con DataFaker: clientes con RUN válido y boletas históricas.
 * Las contraseñas se guardan encriptadas con BCrypt.
 */
@Component
@Profile({"dev", "docker"})
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private static final int CLIENTES_FAKER = 5;
    private static final int BOLETAS_FAKER = 6;

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // Semilla fija: siempre se generan los mismos datos de ejemplo
    private final Faker faker = new Faker(Locale.of("es"), new Random(2026));
    private final Random random = new Random(2026);

    public DataLoader(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                      UsuarioRepository usuarioRepository, BoletaRepository boletaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.boletaRepository = boletaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() > 0) {
            log.info("La base de datos ya tiene datos: no se cargan datos iniciales.");
            return;
        }
        Map<String, Categoria> categorias = cargarCategorias();
        Map<String, Producto> productos = cargarProductos(categorias);
        Map<String, Usuario> usuarios = cargarUsuarios();
        cargarBoletas(usuarios, productos);
        generarClientesFaker();
        generarBoletasFaker(new ArrayList<>(productos.values()));
        log.info("Datos iniciales cargados: {} categorías, {} propiedades, {} usuarios, {} boletas.",
                categoriaRepository.count(), productoRepository.count(),
                usuarioRepository.count(), boletaRepository.count());
    }

    private Map<String, Categoria> cargarCategorias() throws IOException {
        Map<String, Categoria> porNombre = new HashMap<>();
        for (JsonNode n : leer("categorias.json")) {
            Categoria c = categoriaRepository.save(Categoria.builder()
                    .id(n.get("id").asText())
                    .nombre(n.get("nombre").asText())
                    .descripcion(texto(n, "descripcion"))
                    .imagen(texto(n, "imagen"))
                    .build());
            porNombre.put(c.getNombre().toLowerCase(), c);
        }
        return porNombre;
    }

    private Map<String, Producto> cargarProductos(Map<String, Categoria> categorias) throws IOException {
        Map<String, Producto> porId = new LinkedHashMap<>();
        for (JsonNode n : leer("productos.json")) {
            List<String> imagenes = new ArrayList<>();
            n.path("imagenes").forEach(i -> imagenes.add(i.asText()));
            Producto p = productoRepository.save(Producto.builder()
                    .id(n.get("id").asText())
                    .codigo(n.get("codigo").asText())
                    .nombre(n.get("nombre").asText())
                    .descripcion(texto(n, "descripcion"))
                    .precio(n.get("precio").asInt())
                    .precioOferta(n.hasNonNull("precioOferta") ? n.get("precioOferta").asInt() : null)
                    .unidadesDisponibles(n.get("unidadesDisponibles").asInt())
                    .alertaDisponibilidad(n.get("alertaDisponibilidad").asInt())
                    .categoria(categorias.get(n.get("categoria").asText().toLowerCase()))
                    .region(texto(n, "region"))
                    .comuna(texto(n, "comuna"))
                    .direccion(texto(n, "direccion"))
                    .m2(n.path("m2").asInt())
                    .dormitorios(n.path("dormitorios").asInt())
                    .banos(n.path("banos").asInt())
                    .estacionamiento(n.path("estacionamiento").asBoolean(false))
                    .destacada(n.path("destacada").asBoolean(false))
                    .arrendada(false)
                    .estado(texto(n, "estado"))
                    .publicadoPor(texto(n, "publicadoPor"))
                    .imagenes(imagenes)
                    .build());
            porId.put(p.getId(), p);
        }
        return porId;
    }

    private Map<String, Usuario> cargarUsuarios() throws IOException {
        Map<String, Usuario> porCorreo = new HashMap<>();
        for (JsonNode n : leer("usuarios.json")) {
            Usuario u = usuarioRepository.save(Usuario.builder()
                    .run(n.get("run").asText())
                    .nombre(n.get("nombre").asText())
                    .apellidos(n.get("apellidos").asText())
                    .correo(n.get("correo").asText())
                    .clave(passwordEncoder.encode(n.get("clave").asText()))
                    .fechaNacimiento(n.hasNonNull("fechaNacimiento") ? LocalDate.parse(n.get("fechaNacimiento").asText()) : null)
                    .rol(Rol.desdeTexto(n.get("tipoUsuario").asText()))
                    .region(texto(n, "region"))
                    .comuna(texto(n, "comuna"))
                    .direccion(texto(n, "direccion"))
                    .build());
            porCorreo.put(u.getCorreo(), u);
        }
        return porCorreo;
    }

    private void cargarBoletas(Map<String, Usuario> usuarios, Map<String, Producto> productos) throws IOException {
        for (JsonNode n : leer("boletas.json")) {
            JsonNode cli = n.get("cliente");
            JsonNode dir = n.get("direccion");
            Boleta b = Boleta.builder()
                    .numero(n.get("numero").asLong())
                    .fecha(OffsetDateTime.parse(n.get("fecha").asText()).toLocalDateTime())
                    .estado(n.get("estado").asText())
                    .usuario(usuarios.get(n.get("usuarioCorreo").asText()))
                    .clienteNombre(cli.get("nombre").asText())
                    .clienteApellidos(cli.get("apellidos").asText())
                    .clienteCorreo(cli.get("correo").asText())
                    .calle(dir.get("calle").asText())
                    .departamento(texto(dir, "departamento"))
                    .region(dir.get("region").asText())
                    .comuna(dir.get("comuna").asText())
                    .indicaciones(texto(dir, "indicaciones"))
                    .fechaInicio(LocalDate.parse(n.get("fechaInicio").asText()))
                    .tarjeta(n.get("tarjeta").asText())
                    .total(n.get("total").asInt())
                    .codigoAutorizacion(texto(n, "codigoAutorizacion"))
                    .motivoRechazo(texto(n, "motivoRechazo"))
                    .build();
            for (JsonNode item : n.get("items")) {
                b.agregarDetalle(DetalleBoleta.builder()
                        .producto(productos.get(item.get("propiedadId").asText()))
                        .nombre(item.get("nombre").asText())
                        .comuna(texto(item, "comuna"))
                        .imagen(texto(item, "imagen"))
                        .precio(item.get("precio").asInt())
                        .build());
            }
            boletaRepository.save(b);
        }
    }

    /** Clientes de ejemplo con RUN válido (dígito verificador módulo 11) y correo @gmail.com. */
    private void generarClientesFaker() {
        String[] comunas = {"Santiago", "Maipú", "La Florida", "Ñuñoa", "Providencia", "Puente Alto"};
        for (int i = 1; i <= CLIENTES_FAKER; i++) {
            String nombre = faker.name().firstName();
            String apellidos = faker.name().lastName() + " " + faker.name().lastName();
            String cuerpo = String.valueOf(10_000_000 + random.nextInt(15_000_000));
            String run = cuerpo + Validaciones.calcularDv(cuerpo);
            if (usuarioRepository.existsByRun(run)) {
                continue;
            }
            usuarioRepository.save(Usuario.builder()
                    .run(run)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .correo(sinTildes(nombre + "." + apellidos.split(" ")[0]) + i + "@gmail.com")
                    .clave(passwordEncoder.encode("cliente123"))
                    .fechaNacimiento(LocalDate.of(1975 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)))
                    .rol(Rol.CLIENTE)
                    .region("Región Metropolitana de Santiago")
                    .comuna(comunas[random.nextInt(comunas.length)])
                    .direccion(faker.address().streetName() + " " + (100 + random.nextInt(9000)))
                    .build());
        }
    }

    /** Boletas históricas pagadas de los clientes generados, para tener datos en los reportes. */
    private void generarBoletasFaker(List<Producto> productos) {
        List<Usuario> clientes = usuarioRepository.findByRol(Rol.CLIENTE);
        List<Producto> publicadas = productos.stream().filter(p -> "publicada".equals(p.getEstado())).toList();
        if (clientes.isEmpty() || publicadas.isEmpty()) {
            return;
        }
        long numero = boletaRepository.findUltimoNumero();
        for (int i = 0; i < BOLETAS_FAKER; i++) {
            Usuario u = clientes.get(random.nextInt(clientes.size()));
            Producto p = publicadas.get(random.nextInt(publicadas.size()));
            LocalDateTime fecha = LocalDateTime.of(2026, 4 + random.nextInt(6), 1 + random.nextInt(28), 9 + random.nextInt(10), 0);
            Boleta b = Boleta.builder()
                    .numero(++numero)
                    .fecha(fecha)
                    .estado("pagada")
                    .usuario(u)
                    .clienteNombre(u.getNombre())
                    .clienteApellidos(u.getApellidos())
                    .clienteCorreo(u.getCorreo())
                    .calle(u.getDireccion())
                    .region(u.getRegion())
                    .comuna(u.getComuna())
                    .fechaInicio(fecha.toLocalDate().plusMonths(1).withDayOfMonth(1))
                    .tarjeta("•••• 4242")
                    .total(p.precioFinal())
                    .codigoAutorizacion("AUT-" + (100000 + random.nextInt(900000)))
                    .build();
            b.agregarDetalle(DetalleBoleta.builder()
                    .producto(p)
                    .nombre(p.getNombre())
                    .comuna(p.getComuna())
                    .imagen(p.getImagenes().isEmpty() ? null : p.getImagenes().get(0))
                    .precio(p.precioFinal())
                    .build());
            boletaRepository.save(b);
        }
    }

    private JsonNode leer(String archivo) throws IOException {
        try (InputStream in = new ClassPathResource("data/" + archivo).getInputStream()) {
            return mapper.readTree(in);
        }
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull() || v.asText().isBlank()) ? null : v.asText();
    }

    private static String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z.]", "")
                .toLowerCase();
    }
}
