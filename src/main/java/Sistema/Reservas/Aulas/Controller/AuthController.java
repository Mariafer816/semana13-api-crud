package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.RegistroForm;
import Sistema.Reservas.Aulas.Security.CuentaExistenteException;
import Sistema.Reservas.Aulas.Security.GestorSesion;
import Sistema.Reservas.Aulas.Security.RegistroService;
import Sistema.Reservas.Aulas.Security.RegistroService.Resultado;
import Sistema.Reservas.Aulas.Util.Sesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {

    private static final String VISTA_LOGIN = "login";
    private static final String VISTA_REGISTRO = "registro";

    private final RegistroService registroService;
    private final GestorSesion gestorSesion;

    public AuthController(RegistroService registroService, GestorSesion gestorSesion) {
        this.registroService = registroService;
        this.gestorSesion = gestorSesion;
    }

    // ---------- LOGIN ----------
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Authentication authentication,
                        Model model) {
        if (Sesion.estaAutenticado(authentication)) {
            return "redirect:/";
        }
        model.addAttribute("loginError", error != null);
        model.addAttribute("sesionCerrada", logout != null);
        return VISTA_LOGIN;
    }

    // ---------- REGISTRO ----------
    @GetMapping("/registro")
    public String registro(Authentication authentication, Model model) {
        if (Sesion.estaAutenticado(authentication)) {
            return "redirect:/";
        }
        model.addAttribute("registroForm", new RegistroForm());
        return VISTA_REGISTRO;
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult result,
                            RedirectAttributes redirect,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        if (result.hasErrors()) {
            return VISTA_REGISTRO;
        }

        try {
            Resultado resultado = registroService.registrar(form);

            // Cuenta lista: se abre la sesión de inmediato, sin pedirle que vuelva a escribir sus datos
            gestorSesion.iniciarSesion(form.getCorreo().trim().toLowerCase(), request, response);

            redirect.addFlashAttribute("mensaje", mensajeDe(resultado));
            return "redirect:/";   // al menú principal (el mensaje flash se muestra allí)
        } catch (CuentaExistenteException e) {
            result.rejectValue("correo", "correo.duplicado",
                    "Ese correo ya tiene una cuenta con contraseña. Inicie sesión.");
            return VISTA_REGISTRO;
        }
    }

    private String mensajeDe(Resultado resultado) {
        return switch (resultado) {
            case CREADA -> "Cuenta creada correctamente. Bienvenido, ya inició sesión.";
            case ACTIVADA -> "Contraseña creada para su usuario existente. Bienvenido, ya inició sesión.";
        };
    }
}
