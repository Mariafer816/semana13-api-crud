package Sistema.Reservas.Aulas.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Menú principal. Sin sesión, Spring Security envía primero al login. */
    @GetMapping("/")
    public String inicio() {
        return "menu";
    }
}
