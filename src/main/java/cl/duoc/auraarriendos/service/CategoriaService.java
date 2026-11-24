package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.CategoriaDTO;
import cl.duoc.auraarriendos.dto.CategoriaRequest;
import cl.duoc.auraarriendos.exception.RecursoNoEncontradoException;
import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import cl.duoc.auraarriendos.model.Categoria;
import cl.duoc.auraarriendos.repository.CategoriaRepository;
import cl.duoc.auraarriendos.repository.ProductoRepository;
import cl.duoc.auraarriendos.util.Validaciones;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Lógica de negocio de las categorías. */
@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Categoria::getNombre))
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtener(String id) {
        return aDto(buscar(id));
    }

    /** Crea la categoría con un id legible generado desde el nombre (ej: "casa-de-campo"). */
    public CategoriaDTO crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría llamada " + nombre);
        }
        String base = Validaciones.slug(nombre);
        String id = base;
        int n = 2;
        while (categoriaRepository.existsById(id)) {
            id = base + "-" + n++;
        }
        Categoria categoria = Categoria.builder()
                .id(id)
                .nombre(nombre)
                .descripcion(req.descripcion())
                .imagen(req.imagen())
                .build();
        return aDto(categoriaRepository.save(categoria));
    }

    /**
     * Edita la categoría. Como cada producto apunta a la categoría por su id (clave foránea),
     * al cambiar el nombre todas sus propiedades lo muestran actualizado automáticamente.
     */
    public CategoriaDTO actualizar(String id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        String nombre = req.nombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría llamada " + nombre);
                });
        categoria.setNombre(nombre);
        categoria.setDescripcion(req.descripcion());
        categoria.setImagen(req.imagen());
        return aDto(categoriaRepository.save(categoria));
    }

    /** Regla de negocio: no se puede eliminar una categoría que tiene propiedades. */
    public void eliminar(String id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede eliminar la categoría " + categoria.getNombre() + " porque tiene propiedades asociadas");
        }
        categoriaRepository.delete(categoria);
    }

    /** Busca por id o, si no existe, por nombre (el frontend envía el nombre). */
    @Transactional(readOnly = true)
    public Categoria buscarPorIdONombre(String valor) {
        String texto = valor == null ? "" : valor.trim();
        return categoriaRepository.findById(texto)
                .or(() -> categoriaRepository.findByNombreIgnoreCase(texto))
                .orElseThrow(() -> new ReglaNegocioException("La categoría " + valor + " no existe"));
    }

    private Categoria buscar(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría " + id));
    }

    private CategoriaDTO aDto(Categoria c) {
        return Mapper.aDto(c, productoRepository.countByCategoriaId(c.getId()));
    }
}
