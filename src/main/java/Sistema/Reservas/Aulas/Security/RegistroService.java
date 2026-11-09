package Sistema.Reservas.Aulas.Security;

import Sistema.Reservas.Aulas.Model.RegistroForm;
import Sistema.Reservas.Aulas.Model.Usuario;
import Sistema.Reservas.Aulas.Repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RegistroService {

    /** Qué ocurrió al registrar con éxito. */
    public enum Resultado {
        /** El correo no existía: se creó el usuario con su contraseña. */
        CREADA,
        /** El correo ya estaba en el sistema sin contraseña: se le asignó una. */
        ACTIVADA
    }

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea la cuenta o, si el correo ya figuraba sin contraseña (p. ej. usuarios sembrados o creados desde
     * el módulo Usuarios), le asigna la contraseña conservando su nombre y rol.
     *
     * @throws CuentaExistenteException si el correo ya tiene contraseña
     */
    @Transactional
    public Resultado registrar(RegistroForm form) {
        String correo = form.getCorreo().trim().toLowerCase();
        String hash = passwordEncoder.encode(form.getPassword());

        Optional<Usuario> existente = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (existente.isPresent()) {
            return activar(existente.get(), hash);
        }
        return crear(form, correo, hash);
    }

    private Resultado activar(Usuario usuario, String hash) {
        if (usuario.getPassword() != null) {
            throw new CuentaExistenteException(usuario.getCorreo());
        }
        usuario.setPassword(hash);
        usuarioRepository.save(usuario);
        return Resultado.ACTIVADA;
    }

    private Resultado crear(RegistroForm form, String correo, String hash) {
        Usuario nuevo = new Usuario(form.getNombre().trim(), correo, form.getRol());
        nuevo.setPassword(hash);
        try {
            usuarioRepository.save(nuevo);
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo correo: lo detecta la restricción UNIQUE de la BD
            throw new CuentaExistenteException(correo, e);
        }
        return Resultado.CREADA;
    }
}
