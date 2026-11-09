package Sistema.Reservas.Aulas.Repository;

import Sistema.Reservas.Aulas.Model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Para iniciar sesión y registrarse: busca por correo sin distinguir mayúsculas
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Para validar que el correo no esté repetido
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
