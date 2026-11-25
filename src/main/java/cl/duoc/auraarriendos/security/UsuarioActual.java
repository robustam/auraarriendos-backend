package cl.duoc.auraarriendos.security;

import cl.duoc.auraarriendos.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

/** Ayudas para saber quién hace la petición (sacado del token JWT). */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }

    /** Administrador o vendedor. */
    public static boolean esStaff(Authentication auth) {
        return tieneRol(auth, "ADMIN") || tieneRol(auth, "VENDEDOR");
    }

    /** Un cliente solo puede ver lo suyo; el personal puede ver todo. */
    public static void verificarDuenoOStaff(Authentication auth, String correoDueno) {
        if (esStaff(auth)) {
            return;
        }
        if (auth == null || correoDueno == null || !auth.getName().equalsIgnoreCase(correoDueno)) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "No tienes permiso para ver información de otro usuario");
        }
    }
}
