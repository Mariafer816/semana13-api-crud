package Sistema.Reservas.Aulas.Service;

import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Repository.ReservaRepository;
import Sistema.Reservas.Aulas.Repository.UsuarioRepository;
import Sistema.Reservas.Aulas.Util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll(Sort.by("nombre").ascending());
    }

    /** Búsqueda paginada por parte del nombre; sin texto devuelve todos. */
    public Page<Usuario> buscar(String q, int page, int size) {
        String texto = Paginacion.limpiar(q);
        Sort orden = Sort.by("nombre").ascending();

        if (texto.isEmpty()) {
            return Paginacion.consultar(page, size, orden, p -> usuarioRepository.findAll(p));
        }
        return Paginacion.consultar(page, size, orden,
                p -> usuarioRepository.findByNombreContainingIgnoreCase(texto, p));
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /** true si ya hay otro usuario con ese correo (idActual = null al crear). */
    public boolean correoEnUso(String correo, Long idActual) {
        if (idActual == null) {
            return usuarioRepository.existsByCorreoIgnoreCase(correo);
        }
        return usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, idActual);
    }

    public Usuario guardar(Usuario usuario) {
        // El formulario de usuarios no incluye la contraseña: al editar se conserva la que ya tenía.
        if (usuario.getId() != null) {
            usuarioRepository.findById(usuario.getId())
                    .ifPresent(existente -> usuario.setPassword(existente.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        if (reservaRepository.existsByUsuarioId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el usuario porque tiene reservas asociadas.");
        }
        usuarioRepository.deleteById(id);
    }
}
