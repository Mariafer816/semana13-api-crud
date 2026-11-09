package Sistema.Reservas.Aulas.Repository;

import Sistema.Reservas.Aulas.Model.Aula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {

    // Búsqueda por parte del nombre, sin distinguir mayúsculas, con paginación
    Page<Aula> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
