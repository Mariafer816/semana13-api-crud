package Sistema.Reservas.Aulas.Security;

public class CuentaExistenteException extends RuntimeException {

    public CuentaExistenteException(String correo) {
        super("Ya existe una cuenta con el correo " + correo);
    }

    public CuentaExistenteException(String correo, Throwable causa) {
        super("Ya existe una cuenta con el correo " + correo, causa);
    }
}
