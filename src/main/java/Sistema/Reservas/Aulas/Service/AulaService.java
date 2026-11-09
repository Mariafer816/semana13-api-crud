package Sistema.Reservas.Aulas.Service;

import Sistema.Reservas.Aulas.Model.Aula;
import Sistema.Reservas.Aulas.Repository.AulaRepository;
import Sistema.Reservas.Aulas.Repository.ReservaRepository;
import Sistema.Reservas.Aulas.Util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final ReservaRepository reservaRepository;

    public AulaService(AulaRepository aulaRepository, ReservaRepository reservaRepository) {
        this.aulaRepository = aulaRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<Aula> obtenerTodas() {
        return aulaRepository.findAll(Sort.by("nombre").ascending());
    }

    /** Búsqueda paginada: sin texto devuelve el listado completo. */
    public Page<Aula> buscar(String q, int page, int size) {
        String texto = Paginacion.limpiar(q);
        Sort orden = Sort.by("nombre").ascending();

        if (texto.isEmpty()) {
            return Paginacion.consultar(page, size, orden, p -> aulaRepository.findAll(p));
        }
        return Paginacion.consultar(page, size, orden,
                p -> aulaRepository.findByNombreContainingIgnoreCase(texto, p));
    }

    public Aula guardar(Aula aula) {
        return aulaRepository.save(aula);
    }

    public Aula obtenerPorId(Long id) {
        return aulaRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        if (reservaRepository.existsByAulaId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el aula porque tiene reservas asociadas.");
        }
        aulaRepository.deleteById(id);
    }
}
