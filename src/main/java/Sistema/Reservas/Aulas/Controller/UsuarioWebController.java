package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Service.UsuarioService;
import Sistema.Reservas.Aulas.Util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarioService;

    public UsuarioWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // La contraseña solo se crea desde /registro; este formulario nunca debe poder asignarla.
    @InitBinder("usuario")
    public void bloquearPassword(WebDataBinder binder) {
        binder.setDisallowedFields("password");
    }

    // ---------- LISTAR + BUSCAR + PAGINAR ----------
    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         Model model) {
        Page<Usuario> pagina = usuarioService.buscar(q, page, size);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", Paginacion.limpiar(q));
        return "usuarios";
    }

    // ---------- CREAR ----------
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario-form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("usuario") Usuario usuario,
                          BindingResult result,
                          RedirectAttributes redirect) {

        validarCorreoUnico(usuario, null, result);

        if (result.hasErrors()) {
            return "usuario-form";
        }
        usuario.setId(null); // al crear nunca se debe sobrescribir un registro existente
        usuarioService.guardar(usuario);
        redirect.addFlashAttribute("mensaje", "Usuario registrado correctamente.");
        return "redirect:/usuarios";
    }

    // ---------- EDITAR ----------
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    model.addAttribute("usuario", usuario);
                    return "usuario-form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "El usuario no existe.");
                    return "redirect:/usuarios";
                });
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("usuario") Usuario usuario,
                             BindingResult result,
                             RedirectAttributes redirect) {

        if (usuarioService.buscarPorId(id).isEmpty()) {
            redirect.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }

        usuario.setId(id); // el id manda la URL, no el formulario
        validarCorreoUnico(usuario, id, result);

        if (result.hasErrors()) {
            return "usuario-form";
        }
        usuarioService.guardar(usuario);
        redirect.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        return "redirect:/usuarios";
    }

    // ---------- ELIMINAR (POST) ----------
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            redirect.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/usuarios";
        }
        try {
            usuarioService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // Validación propia (no se puede expresar con anotaciones): correo repetido
    private void validarCorreoUnico(Usuario usuario, Long idActual, BindingResult result) {
        if (!result.hasFieldErrors("correo") && usuario.getCorreo() != null
                && usuarioService.correoEnUso(usuario.getCorreo().trim(), idActual)) {
            result.rejectValue("correo", "correo.duplicado",
                    "Ya existe un usuario registrado con ese correo");
        }
    }
}
