package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Curso;
import Sistema.Reservas.Aulas.Model.CursoForm;
import Sistema.Reservas.Aulas.Service.CursoService;
import Sistema.Reservas.Aulas.Util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CursoWebController {

    private final CursoService cursoService;

    public CursoWebController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // ---------- LISTAR + BUSCAR + PAGINAR ----------
    @GetMapping("/cursos")
    public String listar(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         Model model) {
        Page<Curso> pagina = cursoService.buscar(q, page, size);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", Paginacion.limpiar(q));
        return "cursos";
    }

    // ---------- CREAR ----------
    @GetMapping("/cursos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cursoForm", new CursoForm());
        return "curso-form";
    }

    @PostMapping("/cursos")
    public String guardar(@Valid @ModelAttribute("cursoForm") CursoForm form,
                          BindingResult result,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "curso-form";
        }
        cursoService.crear(form.getNombre().trim(), form.getHoras());
        redirect.addFlashAttribute("mensaje", "Curso creado correctamente.");
        return "redirect:/cursos";
    }

    // ---------- EDITAR ----------
    @GetMapping("/cursos/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return cursoService.buscarOpcionalPorId(id)
                .map(curso -> {
                    model.addAttribute("cursoForm", new CursoForm(curso.getNombre(), curso.getHoras()));
                    model.addAttribute("cursoId", id);
                    return "curso-form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "El curso no existe.");
                    return "redirect:/cursos";
                });
    }

    @PostMapping("/cursos/{id}/actualizar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("cursoForm") CursoForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        if (cursoService.buscarOpcionalPorId(id).isEmpty()) {
            redirect.addFlashAttribute("error", "El curso no existe.");
            return "redirect:/cursos";
        }
        if (result.hasErrors()) {
            model.addAttribute("cursoId", id);
            return "curso-form";
        }
        cursoService.actualizar(id, form.getNombre().trim(), form.getHoras());
        redirect.addFlashAttribute("mensaje", "Curso actualizado correctamente.");
        return "redirect:/cursos";
    }

    // ---------- ELIMINAR (POST, no GET) ----------
    @PostMapping("/cursos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        if (cursoService.buscarOpcionalPorId(id).isEmpty()) {
            redirect.addFlashAttribute("error", "El curso no existe.");
            return "redirect:/cursos";
        }
        try {
            cursoService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Curso eliminado correctamente.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }
}
