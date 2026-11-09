package Sistema.Reservas.Aulas.Controller;

import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Service.UsuarioService;
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
import java.util.Set;

/**
 * API REST de Usuarios en /api/usuarios.
 * La contraseña nunca entra ni sale por la API: el acceso se crea desde /registro.
 * 200 correcto | 400 datos inválidos, correo repetido o usuario con reservas | 404 usuario inexistente.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Set<String> ROLES = Set.of("DOCENTE", "ESTUDIANTE", "ADMINISTRATIVO");

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> noEncontrado(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario, BindingResult result) {
        Map<String, String> errores = validar(usuario, result, null);
        if (!errores.isEmpty()) {
            return datosInvalidos(errores);
        }
        usuario.setId(null);
        usuario.setPassword(null);
        usuario.setCorreo(usuario.getCorreo().trim());
        return ResponseEntity.ok(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario detalles,
                                        BindingResult result) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            return noEncontrado(id);
        }
        Map<String, String> errores = validar(detalles, result, id);
        if (!errores.isEmpty()) {
            return datosInvalidos(errores);
        }
        detalles.setId(id);   // guardar() conserva la contraseña que ya tenía
        detalles.setCorreo(detalles.getCorreo().trim());
        return ResponseEntity.ok(usuarioService.guardar(detalles));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            return noEncontrado(id);
        }
        try {
            usuarioService.eliminar(id);
        } catch (IllegalStateException e) {   // el usuario tiene reservas
            return mensaje(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return mensaje(HttpStatus.OK, "Usuario eliminado correctamente");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> solicitudInvalida(Exception ex) {
        return mensaje(HttpStatus.BAD_REQUEST, "Solicitud inválida: JSON mal formado o id con formato incorrecto");
    }

    /** Errores de @Valid + rol permitido + correo no repetido. Vacío si todo está bien. */
    private Map<String, String> validar(Usuario u, BindingResult result, Long idActual) {
        Map<String, String> errores = new LinkedHashMap<>();
        result.getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        if (errores.isEmpty()) {
            if (!ROLES.contains(u.getRol())) {
                errores.put("rol", "El rol debe ser DOCENTE, ESTUDIANTE o ADMINISTRATIVO");
            }
            if (usuarioService.correoEnUso(u.getCorreo().trim(), idActual)) {
                errores.put("correo", "Ya existe un usuario con ese correo");
            }
        }
        return errores;
    }

    // ---------- respuestas JSON ----------
    private ResponseEntity<Map<String, Object>> noEncontrado(Long id) {
        return mensaje(HttpStatus.NOT_FOUND, "No existe un usuario con id " + id);
    }

    private ResponseEntity<Map<String, Object>> datosInvalidos(Map<String, String> errores) {
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
