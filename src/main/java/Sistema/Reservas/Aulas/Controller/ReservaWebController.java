package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Aula;
import Sistema.Reservas.Aulas.Model.Curso;
import Sistema.Reservas.Aulas.Model.Reserva;
import Sistema.Reservas.Aulas.Model.ReservaForm;
import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Service.AulaService;
import Sistema.Reservas.Aulas.Service.CursoService;
import Sistema.Reservas.Aulas.Service.ReservaService;
import Sistema.Reservas.Aulas.Service.UsuarioService;
import Sistema.Reservas.Aulas.Util.Paginacion;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReservaWebController {

    private final ReservaService reservaService;
    private final AulaService aulaService;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;

    public ReservaWebController(
            ReservaService reservaService,
            AulaService aulaService,
            UsuarioService usuarioService,
            CursoService cursoService) {

        this.reservaService = reservaService;
        this.aulaService = aulaService;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
    }

    // ---------- LISTAR ----------

    // Busca por nombre de aula, usuario o curso. Sin texto: listado completo.
    @GetMapping("/reservas")
    public String listarReservas(@RequestParam(defaultValue = "") String q,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 Model model) {
        Page<Reserva> pagina = reservaService.buscar(q, page, size);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", Paginacion.limpiar(q));
        model.addAttribute("baseUrl", "/reservas");
        return "reservas";
    }

    @GetMapping("/aulas/{id}/reservas")
    public String reservasPorAula(@PathVariable Long id,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  Model model,
                                  RedirectAttributes redirect) {
        Aula aula = aulaService.obtenerPorId(id);
        if (aula == null) {
            redirect.addFlashAttribute("error", "El aula no existe.");
            return "redirect:/aulas";
        }
        model.addAttribute("pagina", reservaService.buscarPorAula(id, page, size));
        model.addAttribute("q", "");
        model.addAttribute("aulaFiltro", aula);
        model.addAttribute("baseUrl", "/aulas/" + id + "/reservas");
        return "reservas";
    }

    // ---------- VER DETALLE ----------

    @GetMapping("/reservas/{id}")
    public String verReserva(@PathVariable Long id,
                             Model model,
                             RedirectAttributes redirect) {

        return reservaService.buscarPorId(id)
                .map(reserva -> {
                    model.addAttribute("reserva", reserva);
                    return "reserva-detalle";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "La reserva no existe.");
                    return "redirect:/reservas";
                });
    }

    // ---------- CREAR / GUARDAR ----------

    @GetMapping("/reservas/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("reservaForm", new ReservaForm());
        cargarListas(model);
        return "reserva-form";
    }

    @PostMapping("/reservas/guardar")
    public String guardarReserva(
            @Valid @ModelAttribute("reservaForm") ReservaForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        // Validación cruzada: la hora de fin debe ser posterior a la de inicio
        if (form.getHoraInicio() != null && form.getHoraFin() != null
                && !form.getHoraFin().isAfter(form.getHoraInicio())) {
            result.rejectValue("horaFin", "horario.invalido",
                    "La hora de finalización debe ser posterior a la hora de inicio");
        }

        if (result.hasErrors()) {
            cargarListas(model);
            return "reserva-form";
        }

        Aula aula = aulaService.obtenerPorId(form.getAulaId());
        Usuario usuario = usuarioService.buscarPorId(form.getUsuarioId()).orElse(null);
        Curso curso = cursoService.buscarOpcionalPorId(form.getCursoId()).orElse(null);

        if (aula == null || usuario == null || curso == null) {
            model.addAttribute("errorReserva", "El aula, el usuario o el curso seleccionado no existe.");
            cargarListas(model);
            return "reserva-form";
        }

        Reserva reserva = new Reserva();
        reserva.setAula(aula);
        reserva.setUsuario(usuario);
        reserva.setCurso(curso);
        reserva.setFecha(form.getFecha());
        reserva.setHoraInicio(form.getHoraInicio());
        reserva.setHoraFin(form.getHoraFin());
        reserva.setEstado("CONFIRMADA");

        try {
            reservaService.guardar(reserva);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorReserva", e.getMessage());
            cargarListas(model);
            return "reserva-form";
        }

        redirect.addFlashAttribute("mensaje", "Reserva guardada correctamente.");
        return "redirect:/reservas";
    }

    // ---------- EDITAR ----------

    @GetMapping("/reservas/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirect) {

        return reservaService.buscarPorId(id)
                .map(reserva -> {
                    ReservaForm form = new ReservaForm();
                    form.setAulaId(reserva.getAula().getId());
                    form.setUsuarioId(reserva.getUsuario().getId());
                    if (reserva.getCurso() != null) {
                        form.setCursoId(reserva.getCurso().getId());
                    }
                    form.setFecha(reserva.getFecha());
                    form.setHoraInicio(reserva.getHoraInicio());
                    form.setHoraFin(reserva.getHoraFin());

                    model.addAttribute("reservaForm", form);
                    model.addAttribute("reservaId", id);
                    cargarListas(model);
                    return "reserva-form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "La reserva no existe.");
                    return "redirect:/reservas";
                });
    }

    @PostMapping("/reservas/{id}/actualizar")
    public String actualizarReserva(
            @PathVariable Long id,
            @Valid @ModelAttribute("reservaForm") ReservaForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        Reserva existente = reservaService.buscarPorId(id).orElse(null);
        if (existente == null) {
            redirect.addFlashAttribute("error", "La reserva no existe.");
            return "redirect:/reservas";
        }

        model.addAttribute("reservaId", id);

        if (form.getHoraInicio() != null && form.getHoraFin() != null
                && !form.getHoraFin().isAfter(form.getHoraInicio())) {
            result.rejectValue("horaFin", "horario.invalido",
                    "La hora de finalización debe ser posterior a la hora de inicio");
        }

        if (result.hasErrors()) {
            cargarListas(model);
            return "reserva-form";
        }

        Aula aula = aulaService.obtenerPorId(form.getAulaId());
        Usuario usuario = usuarioService.buscarPorId(form.getUsuarioId()).orElse(null);
        Curso curso = cursoService.buscarOpcionalPorId(form.getCursoId()).orElse(null);

        if (aula == null || usuario == null || curso == null) {
            model.addAttribute("errorReserva", "El aula, el usuario o el curso seleccionado no existe.");
            cargarListas(model);
            return "reserva-form";
        }

        existente.setAula(aula);
        existente.setUsuario(usuario);
        existente.setCurso(curso);
        existente.setFecha(form.getFecha());
        existente.setHoraInicio(form.getHoraInicio());
        existente.setHoraFin(form.getHoraFin());

        try {
            reservaService.guardar(existente);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorReserva", e.getMessage());
            cargarListas(model);
            return "reserva-form";
        }

        redirect.addFlashAttribute("mensaje", "Reserva actualizada correctamente.");
        return "redirect:/reservas";
    }

    // ---------- ELIMINAR ----------

    @PostMapping("/reservas/{id}/eliminar")
    public String eliminarReserva(@PathVariable Long id, RedirectAttributes redirect) {

        if (reservaService.buscarPorId(id).isPresent()) {
            reservaService.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Reserva eliminada correctamente.");
        } else {
            redirect.addFlashAttribute("error", "La reserva no existe.");
        }
        return "redirect:/reservas";
    }

    private void cargarListas(Model model) {
        model.addAttribute("aulas", aulaService.obtenerTodas());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("cursos", cursoService.listar());
    }
}
