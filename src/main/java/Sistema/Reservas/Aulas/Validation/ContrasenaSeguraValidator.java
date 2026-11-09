package Sistema.Reservas.Aulas.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Reglas: mínimo 8 caracteres, al menos una letra y un número, y máximo 72 bytes
 * (BCrypt solo procesa los primeros 72 bytes). Un valor vacío lo rechaza @NotBlank, no este validador.
 */
public class ContrasenaSeguraValidator implements ConstraintValidator<ContrasenaSegura, String> {

    private static final int LONGITUD_MINIMA = 8;
    private static final int MAXIMO_BYTES = 72;
    private static final Pattern TIENE_LETRA = Pattern.compile("\\p{L}");
    private static final Pattern TIENE_NUMERO = Pattern.compile("\\d");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }

        String error = null;
        if (password.length() < LONGITUD_MINIMA) {
            error = "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres";
        } else if (password.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            error = "La contraseña es demasiado larga (use menos tildes o símbolos)";
        } else if (!TIENE_LETRA.matcher(password).find() || !TIENE_NUMERO.matcher(password).find()) {
            error = "La contraseña debe incluir al menos una letra y un número";
        }

        if (error == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(error).addConstraintViolation();
        return false;
    }
}
