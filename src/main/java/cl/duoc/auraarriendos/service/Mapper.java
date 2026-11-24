package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.BoletaDTO;
import cl.duoc.auraarriendos.dto.CategoriaDTO;
import cl.duoc.auraarriendos.dto.ProductoDTO;
import cl.duoc.auraarriendos.dto.UsuarioDTO;
import cl.duoc.auraarriendos.model.Boleta;
import cl.duoc.auraarriendos.model.Categoria;
import cl.duoc.auraarriendos.model.Producto;
import cl.duoc.auraarriendos.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/** Convierte las entidades de la base de datos en los DTO que devuelve la API. */
public final class Mapper {

    private Mapper() {
    }

    public static ProductoDTO aDto(Producto p) {
        List<String> imagenes = p.getImagenes() == null ? List.of() : new ArrayList<>(p.getImagenes());
        String imagen = imagenes.isEmpty() ? "/img/prop-1.jpg" : imagenes.get(0);
        return new ProductoDTO(
                p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getPrecioOferta(), p.getUnidadesDisponibles(), p.getAlertaDisponibilidad(),
                p.getCategoria().getNombre(), p.getCategoria().getId(),
                p.getRegion(), p.getComuna(), p.getDireccion(),
                p.getM2(), p.getDormitorios(), p.getBanos(),
                p.getEstacionamiento(), p.getDestacada(), p.getArrendada(), p.getFechaArrendada(),
                p.getEstado(), p.getMotivoRechazo(), p.getPublicadoPor(),
                imagen, imagenes);
    }

    public static CategoriaDTO aDto(Categoria c, long cantidadProductos) {
        return new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion(), c.getImagen(), cantidadProductos);
    }

    public static UsuarioDTO aDto(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getFechaNacimiento(), u.getRol().getEtiqueta(), u.getRegion(), u.getComuna(), u.getDireccion());
    }

    public static BoletaDTO aDto(Boleta b) {
        List<BoletaDTO.Item> items = b.getDetalles().stream()
                .map(d -> new BoletaDTO.Item(d.getId(), d.getProducto().getId(), d.getNombre(),
                        d.getComuna(), d.getImagen(), d.getPrecio()))
                .toList();
        return new BoletaDTO(
                b.getId(), b.getNumero(), b.getFecha(), b.getEstado(),
                new BoletaDTO.Cliente(b.getClienteNombre(), b.getClienteApellidos(), b.getClienteCorreo()),
                new BoletaDTO.Direccion(b.getCalle(), b.getDepartamento(), b.getRegion(), b.getComuna(), b.getIndicaciones()),
                b.getFechaInicio(), b.getTarjeta(), b.getCodigoAutorizacion(), b.getMotivoRechazo(),
                b.getUsuario().getCorreo(), items, b.getTotal());
    }
}
