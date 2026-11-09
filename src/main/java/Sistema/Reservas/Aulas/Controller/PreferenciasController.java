package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Service.PreferenciasService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Guarda las preferencias del usuario en cookies. Controller → Service
 */
@Controller
@RequestMapping("/preferencias")
public class PreferenciasController {

    private final PreferenciasService preferenciasService;

    public PreferenciasController(PreferenciasService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    // POST (y no GET) porque cambia el estado; Thymeleaf agrega el token CSRF al formulario del menú
    @PostMapping("/tema")
    public String cambiarTema(@RequestParam String valor,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                preferenciasService.crearCookieTema(valor, request.isSecure()).toString());

        return "redirect:" + preferenciasService.rutaDeRetorno(request.getHeader(HttpHeaders.REFERER));
    }
}
