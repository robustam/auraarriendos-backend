package cl.duoc.auraarriendos.model;

/**
 * Roles del sistema (Anexo 1 de la evaluación):
 * - ADMIN: acceso total.
 * - VENDEDOR: solo ve productos y órdenes (lista y detalle).
 * - CLIENTE: solo accede a la tienda.
 */
public enum Rol {
    ADMIN("Administrador"),
    VENDEDOR("Vendedor"),
    CLIENTE("Cliente");

    /** Nombre que muestra el frontend. */
    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Acepta tanto "ADMIN" como "Administrador" (sin importar mayúsculas). */
    public static Rol desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return CLIENTE;
        }
        for (Rol rol : values()) {
            if (rol.name().equalsIgnoreCase(texto.trim()) || rol.etiqueta.equalsIgnoreCase(texto.trim())) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Tipo de usuario no válido: " + texto);
    }
}
