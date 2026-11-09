package Sistema.Reservas.Aulas.Validation;

import Sistema.Reservas.Aulas.Model.RegistroForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** El error se asocia al campo "confirmarPassword" para que se muestre debajo de él. */
public class PasswordsCoincidenValidator implements ConstraintValidator<PasswordsCoinciden, RegistroForm> {

    @Override
    public boolean isValid(RegistroForm form, ConstraintValidatorContext context) {
        if (form == null || estaVacio(form.getPassword()) || estaVacio(form.getConfirmarPassword())) {
            return true; // los campos vacíos ya los reporta @NotBlank
        }
        if (form.getPassword().equals(form.getConfirmarPassword())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("confirmarPassword")
                .addConstraintViolation();
        return false;
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
