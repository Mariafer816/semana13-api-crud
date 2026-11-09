package Sistema.Reservas.Aulas.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    // El regexp exige algo como usuario@dominio.ext (por defecto @Email acepta "a@b")
    @NotBlank(message = "El correo es obligatorio")
    @Email(regexp = ".+@.+\\..+", message = "El correo no tiene un formato válido (ej: nombre@dominio.com)")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres")
    @Column(unique = true)
    private String correo;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    // Hash BCrypt (nunca la contraseña en texto plano). Es null mientras el usuario no ha creado su acceso.
    // @JsonIgnore: el hash no debe salir nunca en las respuestas de la API REST.
    @JsonIgnore
    @Column(length = 100)
    private String password;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
