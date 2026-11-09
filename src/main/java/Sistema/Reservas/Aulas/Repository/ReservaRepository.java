package Sistema.Reservas.Aulas.Repository;

import Sistema.Reservas.Aulas.Model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByFecha(LocalDate fecha);

    List<Reserva> findByAulaId(Long aulaId);

    // Versión paginada (para la pantalla de reservas de un aula)
    Page<Reserva> findByAulaId(Long aulaId, Pageable pageable);

    // Búsqueda: coincide con parte del nombre del aula, del usuario o del curso.
    // LEFT JOIN con curso para no perder reservas antiguas que no tienen curso.
    @Query(value = "SELECT r FROM Reserva r JOIN r.aula a JOIN r.usuario u LEFT JOIN r.curso c "
            + "WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%'))",
            countQuery = "SELECT COUNT(r) FROM Reserva r JOIN r.aula a JOIN r.usuario u LEFT JOIN r.curso c "
                    + "WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) "
                    + "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%')) "
                    + "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Reserva> buscar(@Param("q") String q, Pageable pageable);

    // Para impedir borrar aulas / usuarios / cursos que ya tienen reservas
    boolean existsByAulaId(Long aulaId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByCursoId(Long cursoId);

    boolean existsByAulaIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
            Long aulaId,
            LocalDate fecha,
            LocalTime horaFin,
            LocalTime horaInicio
    );

    // Igual que el anterior pero ignorando la propia reserva (para editar)
    boolean existsByAulaIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
            Long aulaId,
            LocalDate fecha,
            LocalTime horaFin,
            LocalTime horaInicio,
            Long id
    );
}
