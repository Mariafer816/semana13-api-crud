package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Curso;
import Sistema.Reservas.Aulas.Service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST de Cursos en /api/cursos.
 * 200 correcto | 400 datos inválidos o curso con reservas | 404 curso inexistente.
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<List<Curso>> listar() {
        return ResponseEntity.ok(cursoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return cursoService.buscarOpcionalPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> noEncontrado(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Curso curso, BindingResult result) {
        if (result.hasErrors()) {
            return datosInvalidos(result);
        }
        return ResponseEntity.ok(cursoService.crear(curso.getNombre().trim(), curso.getHoras()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Curso detalles,
                                        BindingResult result) {
        if (cursoService.buscarOpcionalPorId(id).isEmpty()) {
            return noEncontrado(id);
        }
        if (result.hasErrors()) {
            return datosInvalidos(result);
        }
        return ResponseEntity.ok(cursoService.actualizar(id, detalles.getNombre().trim(), detalles.getHoras()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (cursoService.buscarOpcionalPorId(id).isEmpty()) {
            return noEncontrado(id);
        }
        try {
            cursoService.eliminar(id);
        } catch (IllegalStateException e) {   // el curso tiene reservas
            return mensaje(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return mensaje(HttpStatus.OK, "Curso eliminado correctamente");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> solicitudInvalida(Exception ex) {
        return mensaje(HttpStatus.BAD_REQUEST, "Solicitud inválida: JSON mal formado o id con formato incorrecto");
    }

    // ---------- respuestas JSON ----------
    private ResponseEntity<Map<String, Object>> noEncontrado(Long id) {
        return mensaje(HttpStatus.NOT_FOUND, "No existe un curso con id " + id);
    }

    private ResponseEntity<Map<String, Object>> datosInvalidos(BindingResult result) {
        Map<String, String> errores = new LinkedHashMap<>();
        result.getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", 400);
        cuerpo.put("mensaje", "Los datos enviados no son válidos");
        cuerpo.put("errores", errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    private ResponseEntity<Map<String, Object>> mensaje(HttpStatus estado, String texto) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", texto);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
