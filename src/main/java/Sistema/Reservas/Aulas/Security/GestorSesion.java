package Sistema.Reservas.Aulas.Security;

import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class GestorSesion {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDetailsService usuarioDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public GestorSesion(UsuarioRepository usuarioRepository, UsuarioDetailsService usuarioDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    /** Guarda en la sesión el nombre del usuario; si estuviera vacío usa el correo. */
    public void guardarNombre(HttpServletRequest request, String correo) {
        String nombre = usuarioRepository.findByCorreoIgnoreCase(correo)
                .map(Usuario::getNombre)
                .filter(n -> !n.isBlank())
                .orElse(correo);

        request.getSession().setAttribute(LoginSuccessHandler.ATRIBUTO_NOMBRE, nombre);
    }

    /** Autentica al usuario (ya validado) y crea su sesión: queda "dentro" igual que si hubiera hecho login. */
    public void iniciarSesion(String correo, HttpServletRequest request, HttpServletResponse response) {
        UserDetails usuario = usuarioDetailsService.loadUserByUsername(correo);
        Authentication autenticacion =
                UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);

        if (request.getSession(false) != null) {
            request.changeSessionId();   // id nuevo al cambiar de anónimo a autenticado (anti session-fixation)
        }
        securityContextRepository.saveContext(contexto, request, response);   // lo guarda en la HttpSession
        guardarNombre(request, usuario.getUsername());
    }
}
