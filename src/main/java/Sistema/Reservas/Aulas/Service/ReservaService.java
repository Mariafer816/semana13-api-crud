package Sistema.Reservas.Aulas.Service;

import  Sistema.Reservas.Aulas.Model.Reserva;
import  Sistema.Reservas.Aulas.Repository.ReservaRepository;
import Sistema.Reservas.Aulas.Util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    private static final Sort ORDEN = Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("horaInicio"));

    /** Búsqueda paginada por aula, usuario o curso; sin texto devuelve todas. */
    public Page<Reserva> buscar(String q, int page, int size) {
        String texto = Paginacion.limpiar(q);

        if (texto.isEmpty()) {
            return Paginacion.consultar(page, size, ORDEN, p -> reservaRepository.findAll(p));
        }
        return Paginacion.consultar(page, size, ORDEN, p -> reservaRepository.buscar(texto, p));
    }

    /** Reservas de un aula, paginadas. */
    public Page<Reserva> buscarPorAula(Long aulaId, int page, int size) {
        return Paginacion.consultar(page, size, ORDEN, p -> reservaRepository.findByAulaId(aulaId, p));
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> buscarPorFecha(java.time.LocalDate fecha) {
        return reservaRepository.findByFecha(fecha);
    }

    public List<Reserva> buscarPorAula(Long aulaId) {
        return reservaRepository.findByAulaId(aulaId);
    }

    public Reserva guardar(Reserva reserva) {

        boolean existe;

        if (reserva.getId() == null) {
            existe = reservaRepository
                    .existsByAulaIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                            reserva.getAula().getId(),
                            reserva.getFecha(),
                            reserva.getHoraFin(),
                            reserva.getHoraInicio()
                    );
        } else {
            // Edición: se ignora la propia reserva al buscar cruces de horario
            existe = reservaRepository
                    .existsByAulaIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                            reserva.getAula().getId(),
                            reserva.getFecha(),
                            reserva.getHoraFin(),
                            reserva.getHoraInicio(),
                            reserva.getId()
                    );
        }

        if (existe) {
            throw new IllegalArgumentException(
                    "El aula ya está reservada en ese horario."
            );
        }

        return reservaRepository.save(reserva);
    }

    public void eliminar(Long id) {
        reservaRepository.deleteById(id);
    }
}