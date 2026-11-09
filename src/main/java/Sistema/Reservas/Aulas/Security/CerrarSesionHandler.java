package Sistema.Reservas.Aulas.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CerrarSesionHandler implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);   // null si ya no hay sesión (o ya fue invalidada)
        if (session != null) {
            session.removeAttribute(LoginSuccessHandler.ATRIBUTO_NOMBRE);
            session.invalidate();
        }
    }
}
