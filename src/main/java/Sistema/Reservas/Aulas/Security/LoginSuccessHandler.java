package Sistema.Reservas.Aulas.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /** Clave del atributo en la sesión. */
    public static final String ATRIBUTO_NOMBRE = "usuarioNombre";

    private final GestorSesion gestorSesion;

    public LoginSuccessHandler(GestorSesion gestorSesion) {
        this.gestorSesion = gestorSesion;
        // Al ingresar siempre se llega al menú principal, sin desvíos
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Spring Security ya renovó el id de sesión antes de llegar aquí (anti session-fixation)
        gestorSesion.guardarNombre(request, authentication.getName());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
