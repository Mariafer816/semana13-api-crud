package Sistema.Reservas.Aulas.Util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/** Utilidades para consultar el estado de la sesión actual. */
public final class Sesion {

    private Sesion() {
    }
    public static boolean estaAutenticado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
