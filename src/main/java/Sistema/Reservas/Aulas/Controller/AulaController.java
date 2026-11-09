package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Aula;
import Sistema.Reservas.Aulas.Service.AulaService;
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
 * API REST de Aulas en /api/aulas.
 * 200 correcto | 400 datos inválidos | 404 aula inexistente.
 */
@RestController
@RequestMapping("/api/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    public ResponseEntity<List<Aula>> obtenerTodas() {
        return ResponseEntity.ok(aulaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Aula aula = aulaService.obtenerPorId(id);
        if (aula == null) {
            return noEncontrada(id);
        }
        return ResponseEntity.ok(aula);
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Aula aula, BindingResult result) {
        if (result.hasErrors()) {
            return datosInvalidos(result);
        }
        aula.setId(null);   // el id lo genera la base de datos
        return ResponseEntity.ok(aulaService.guardar(aula));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Aula detalles,
                                        BindingResult result) {
        Aula existente = aulaService.obtenerPorId(id);
        if (existente == null) {
            return noEncontrada(id);
        }
        if (result.hasErrors()) {
            return datosInvalidos(result);
        }
        existente.setNombre(detalles.getNombre());
        existente.setCapacidad(detalles.getCapacidad());
        existente.setUbicacion(detalles.getUbicacion());
        existente.setTipo(detalles.getTipo());
        existente.setDisponible(detalles.isDisponible());
        return ResponseEntity.ok(aulaService.guardar(existente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (aulaService.obtenerPorId(id) == null) {
            return noEncontrada(id);
        }
        try {
            aulaService.eliminar(id);
        } catch (IllegalStateException e) {   // el aula tiene reservas
            return mensaje(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return mensaje(HttpStatus.OK, "Aula eliminada correctamente");
    }

    // JSON mal formado, o un id que no es número (/api/aulas/abc)
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> solicitudInvalida(Exception ex) {
        return mensaje(HttpStatus.BAD_REQUEST, "Solicitud inválida: JSON mal formado o id con formato incorrecto");
    }

    // ---------- respuestas JSON ----------
    private ResponseEntity<Map<String, Object>> noEncontrada(Long id) {
        return mensaje(HttpStatus.NOT_FOUND, "No existe un aula con id " + id);
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
