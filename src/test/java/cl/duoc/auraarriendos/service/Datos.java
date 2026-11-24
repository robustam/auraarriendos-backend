package cl.duoc.auraarriendos.service;

import cl.duoc.auraarriendos.dto.BoletaRequest;
import cl.duoc.auraarriendos.dto.ProductoRequest;
import cl.duoc.auraarriendos.dto.UsuarioRequest;
import cl.duoc.auraarriendos.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Datos de prueba reutilizables (los mismos del sitio). */
public final class Datos {

    private Datos() {
    }

    public static Categoria departamento() {
        return Categoria.builder().id("departamento").nombre("Departamento")
                .descripcion("Departamentos cerca del metro").imagen("/img/prop-3.jpg").build();
    }

    public static Producto producto(String id, int precio, Integer oferta, int stock) {
        return Producto.builder()
                .id(id).codigo(id).nombre("Departamento " + id).descripcion("Descripción")
                .precio(precio).precioOferta(oferta).unidadesDisponibles(stock).alertaDisponibilidad(1)
                .categoria(departamento())
                .region("Región Metropolitana de Santiago").comuna("Providencia").direccion("Av. Siempre Viva 123")
                .m2(55).dormitorios(2).banos(1)
                .estado("publicada").publicadoPor("admin@duoc.cl")
                .imagenes(new ArrayList<>(List.of("/img/prop-1-1.jpg")))
                .build();
    }

    public static ProductoRequest productoRequest(int precio, Integer oferta) {
        return new ProductoRequest(null, "Casa con patio en La Reina", "Linda casa", "Departamento",
                "Región Metropolitana de Santiago", "La Reina", "Av. Larraín 6100", 95,
                precio, oferta, 2, 1, 3, 2, true, false, false, null, null, "cliente@gmail.com",
                "/img/prop-1.jpg", null);
    }

    public static Usuario cliente() {
        return Usuario.builder().id(3L).run("333333333").nombre("Matías").apellidos("Fernández Lira")
                .correo("cliente@gmail.com").clave("$2a$10$hash").rol(Rol.CLIENTE)
                .region("Región Metropolitana de Santiago").comuna("La Florida").direccion("Av. Vicuña Mackenna 8900")
                .build();
    }

    public static UsuarioRequest usuarioRequest(String run, String correo, String clave) {
        return new UsuarioRequest(run, "Camila", "Rojas Pérez", correo, clave, LocalDate.of(1995, 3, 10),
                "Cliente", "Región Metropolitana de Santiago", "Ñuñoa", "Irarrázaval 3000");
    }

    public static BoletaRequest boletaRequest(List<String> ids, String tarjeta, LocalDate inicio) {
        return new BoletaRequest("cliente@gmail.com", ids,
                new BoletaRequest.Cliente("Matías", "Fernández Lira", "cliente@gmail.com"),
                new BoletaRequest.Direccion("Av. Vicuña Mackenna 8900", "", "Región Metropolitana de Santiago", "La Florida", ""),
                inicio,
                new BoletaRequest.Tarjeta("Matías Fernández", tarjeta, "12/28", "123"));
    }
}
