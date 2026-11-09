package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Aula;
import Sistema.Reservas.Aulas.Service.AulaService;
import Sistema.Reservas.Aulas.Util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/aulas")
public class AulaWebController {

    private final AulaService aulaService;

    public AulaWebController(AulaService aulaService) {
        this.aulaService = aulaService;
    }


    @GetMapping
    public String listarAulas(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "5") int size,
                              Model model) {
        Page<Aula> pagina = aulaService.buscar(q, page, size);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", Paginacion.limpiar(q));
        return "aulas";
    }

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("aula", new Aula());
        return "aula-form";
    }

    @PostMapping("/guardar")
    public String guardarAula(@Valid @ModelAttribute("aula") Aula aula,
                              BindingResult result,
                              RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "aula-form";
        }

        boolean esNueva = aula.getId() == null;
        aulaService.guardar(aula);

        redirect.addFlashAttribute("mensaje",
                esNueva ? "Aula registrada correctamente." : "Aula actualizada correctamente.");
        return "redirect:/aulas";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirect) {
        Aula aula = aulaService.obtenerPorId(id);
        if (aula == null) {
            redirect.addFlashAttribute("error", "El aula no existe.");
            return "redirect:/aulas";
        }
        model.addAttribute("aula", aula);
        return "aula-form";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarAula(@PathVariable Long id, RedirectAttributes redirect) {
        if (aulaService.obtenerPorId(id) == null) {
            redirect.addFlashAttribute("error", "El aula no existe.");
            return "redirect:/aulas";
        }
        try {
            aulaService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Aula eliminada correctamente.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/aulas";
    }
}
