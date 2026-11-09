package Sistema.Reservas.Aulas.Model;

import Sistema.Reservas.Aulas.Validation.ContrasenaSegura;
import Sistema.Reservas.Aulas.Validation.PasswordsCoinciden;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordsCoinciden
public class RegistroForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(regexp = ".+@.+\\..+", message = "El correo no tiene un formato válido (ej: nombre@dominio.com)")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres")
    private String correo;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "DOCENTE|ESTUDIANTE|ADMINISTRATIVO", message = "Seleccione un rol válido")
    private String rol;

    @NotBlank(message = "La contraseña es obligatoria")
    @ContrasenaSegura
    private String password;

    @NotBlank(message = "Confirme la contraseña")
    private String confirmarPassword;

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

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }
}
