package Sistema.Reservas.Aulas.Service;

import Sistema.Reservas.Aulas.Model.CursoDto;
import Sistema.Reservas.Aulas.Model.Curso;
import Sistema.Reservas.Aulas.Repository.CursoRepository;
import Sistema.Reservas.Aulas.Repository.ReservaRepository;
import Sistema.Reservas.Aulas.Util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final ReservaRepository reservaRepository;

    public CursoService(CursoRepository cursoRepository, ReservaRepository reservaRepository) {
        this.cursoRepository = cursoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return cursoRepository.findAll(Sort.by("nombre").ascending());
    }

    @Transactional(readOnly = true)
    public Optional<Curso> buscarOpcionalPorId(Long id) {
        return cursoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con el ID: " + id));
    }

    @Transactional
    public Curso crear(String nombre, int horas) {
        return cursoRepository.save(new Curso(nombre, horas));
    }

    @Transactional
    public Curso actualizar(Long id, String nombre, int horas) {
        Curso curso = buscarPorId(id);
        curso.setNombre(nombre);
        curso.setHoras(horas);
        return cursoRepository.save(curso);
    }

    @Transactional
    public void eliminar(Long id) {
        Curso curso = buscarPorId(id);
        if (reservaRepository.existsByCursoId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el curso porque tiene reservas asociadas.");
        }
        cursoRepository.delete(curso);
    }

    /** Búsqueda paginada por parte del nombre; sin texto devuelve todos. */
    @Transactional(readOnly = true)
    public Page<Curso> buscar(String q, int page, int size) {
        String texto = Paginacion.limpiar(q);
        Sort orden = Sort.by("nombre").ascending();

        if (texto.isEmpty()) {
            return Paginacion.consultar(page, size, orden, p -> cursoRepository.findAll(p));
        }
        return Paginacion.consultar(page, size, orden,
                p -> cursoRepository.findByNombreContainingIgnoreCase(texto, p));
    }

    @Transactional(readOnly = true)
    public List<CursoDto> listarDto() {
        return listar().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CursoDto buscarDtoPorId(Long id) {
        return convertirADto(buscarPorId(id));
    }

    @Transactional
    public CursoDto crearDto(String nombre, int horas) {
        return convertirADto(crear(nombre, horas));
    }

    @Transactional
    public CursoDto actualizarDto(Long id, String nombre, int horas) {
        return convertirADto(actualizar(id, nombre, horas));
    }

    private CursoDto convertirADto(Curso curso) {
        return new CursoDto(curso.getId(), curso.getNombre(), curso.getHoras());
    }
}