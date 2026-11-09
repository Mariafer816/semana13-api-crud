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
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST de Reservas en /api/reservas.
 *
 * Cuerpo de POST/PUT (ids de las relaciones, igual que el formulario web):
 *   { "aulaId": 1, "usuarioId": 1, "cursoId": 1, "fecha": "2026-12-15", "horaInicio": "08:00", "horaFin": "10:00" }
 *
 * 200 correcto | 400 datos inválidos, relación inexistente o aula ocupada | 404 reserva inexistente.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final AulaService aulaService;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;

    public ReservaController(ReservaService reservaService, AulaService aulaService,
                             UsuarioService usuarioService, CursoService cursoService) {
        this.reservaService = reservaService;
        this.aulaService = aulaService;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listar() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return reservaService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> noEncontrada(id));
    }

    /** GET /api/reservas/fecha/2026-12-15 (formato AAAA-MM-DD) */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Reserva>> buscarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.buscarPorFecha(fecha));
    }

    @GetMapping("/aula/{aulaId}")
    public ResponseEntity<List<Reserva>> buscarPorAula(@PathVariable Long aulaId) {
        return ResponseEntity.ok(reservaService.buscarPorAula(aulaId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ReservaForm form, BindingResult result) {
        Reserva reserva = new Reserva();
        ResponseEntity<?> error = armar(reserva, form, result);
        if (error != null) {
            return error;
        }
        try {
            return ResponseEntity.ok(reservaService.guardar(reserva));
        } catch (IllegalArgumentException e) {   // aula ocupada en ese horario
            return mensaje(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ReservaForm form,
                                        BindingResult result) {
        Reserva existente = reservaService.buscarPorId(id).orElse(null);
        if (existente == null) {
            return noEncontrada(id);
        }
        ResponseEntity<?> error = armar(existente, form, result);
        if (error != null) {
            return error;
        }
        try {
            return ResponseEntity.ok(reservaService.guardar(existente));
        } catch (IllegalArgumentException e) {   // aula ocupada en ese horario
            return mensaje(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (reservaService.buscarPorId(id).isEmpty()) {
            return noEncontrada(id);
        }
        reservaService.eliminar(id);
        return mensaje(HttpStatus.OK, "Reserva eliminada correctamente");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> solicitudInvalida(Exception ex) {
        return mensaje(HttpStatus.BAD_REQUEST,
                "Solicitud inválida: JSON mal formado, id o fecha con formato incorrecto");
    }

    /**
     * Copia el formulario a la reserva comprobando @Valid, el horario y que existan aula, usuario y curso.
     * Devuelve null si todo está bien o la respuesta 400 con el detalle.
     */
    private ResponseEntity<?> armar(Reserva reserva, ReservaForm form, BindingResult result) {
        Map<String, String> errores = new LinkedHashMap<>();
        result.getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));

        if (errores.isEmpty()) {
            if (!form.getHoraFin().isAfter(form.getHoraInicio())) {
                errores.put("horaFin", "La hora de finalización debe ser posterior a la hora de inicio");
            }
            if (aulaService.obtenerPorId(form.getAulaId()) == null) {
                errores.put("aulaId", "No existe un aula con id " + form.getAulaId());
            }
            if (usuarioService.buscarPorId(form.getUsuarioId()).isEmpty()) {
                errores.put("usuarioId", "No existe un usuario con id " + form.getUsuarioId());
            }
            if (cursoService.buscarOpcionalPorId(form.getCursoId()).isEmpty()) {
                errores.put("cursoId", "No existe un curso con id " + form.getCursoId());
            }
        }
        if (!errores.isEmpty()) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("estado", 400);
            cuerpo.put("mensaje", "Los datos enviados no son válidos");
            cuerpo.put("errores", errores);
            return ResponseEntity.badRequest().body(cuerpo);
        }

        Aula aula = aulaService.obtenerPorId(form.getAulaId());
        Usuario usuario = usuarioService.buscarPorId(form.getUsuarioId()).get();
        Curso curso = cursoService.buscarOpcionalPorId(form.getCursoId()).get();
        reserva.setAula(aula);
        reserva.setUsuario(usuario);
        reserva.setCurso(curso);
        reserva.setFecha(form.getFecha());
        reserva.setHoraInicio(form.getHoraInicio());
        reserva.setHoraFin(form.getHoraFin());
        return null;
    }

    // ---------- respuestas JSON ----------
    private ResponseEntity<Map<String, Object>> noEncontrada(Long id) {
        return mensaje(HttpStatus.NOT_FOUND, "No existe una reserva con id " + id);
    }

    private ResponseEntity<Map<String, Object>> mensaje(HttpStatus estado, String texto) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", texto);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
