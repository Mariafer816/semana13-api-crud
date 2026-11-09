package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Security.LoginSuccessHandler;
import Sistema.Reservas.Aulas.Service.PreferenciasService;
import Sistema.Reservas.Aulas.Util.Sesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class ModeloGlobalAdvice {

    private final PreferenciasService preferenciasService;

    public ModeloGlobalAdvice(PreferenciasService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    /** Cookie "tema" -> "claro" u "oscuro" (por defecto "claro"). */
    @ModelAttribute("tema")
    public String tema(@CookieValue(name = PreferenciasService.COOKIE_TEMA, required = false) String valor) {
        return preferenciasService.normalizarTema(valor);
    }

    /** Nombre guardado en HttpSession al iniciar sesión; si faltara, el correo con el que se autenticó. */
    @ModelAttribute("usuarioActivo")
    public String usuarioActivo(HttpServletRequest request, Authentication authentication) {
        if (!Sesion.estaAutenticado(authentication)) {
            return null;
        }
        HttpSession session = request.getSession(false);   // false: no crear una sesión solo por mirar
        Object nombre = session != null ? session.getAttribute(LoginSuccessHandler.ATRIBUTO_NOMBRE) : null;
        return nombre != null ? nombre.toString() : authentication.getName();
    }
}
