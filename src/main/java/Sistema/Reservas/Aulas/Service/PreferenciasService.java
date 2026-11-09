package Sistema.Reservas.Aulas.Service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;

/**
 * Preferencias del usuario que viven en el NAVEGADOR (cookies), no en la base de datos.
 * Hoy: el tema visual (claro / oscuro).
 */
@Service
public class PreferenciasService {

    public static final String COOKIE_TEMA = "tema";
    public static final String TEMA_CLARO = "claro";
    public static final String TEMA_OSCURO = "oscuro";

    private static final Duration DURACION_COOKIE = Duration.ofDays(365);

    /** Solo se aceptan dos valores; cualquier otra cosa (o ausencia de cookie) equivale a "claro". */
    public String normalizarTema(String valor) {
        return valor != null && TEMA_OSCURO.equalsIgnoreCase(valor.trim()) ? TEMA_OSCURO : TEMA_CLARO;
    }

    /**
     * Construye la cookie de tema.
     * - HttpOnly: JavaScript no puede leerla (el servidor es quien la usa para pintar la página).
     * - SameSite=Lax: el navegador no la envía en peticiones cruzadas iniciadas por otros sitios.
     * - Secure solo si la petición llegó por HTTPS (en local con http no se enviaría).
     * - maxAge de un año: sobrevive al cierre del navegador y también al cierre de sesión.
     */
    public ResponseCookie crearCookieTema(String valor, boolean conexionSegura) {
        return ResponseCookie.from(COOKIE_TEMA, normalizarTema(valor))
                .path("/")
                .maxAge(DURACION_COOKIE)
                .httpOnly(true)
                .secure(conexionSegura)
                .sameSite("Lax")
                .build();
    }

    /**
     * Ruta (solo path + query) a la que volver después de cambiar el tema, tomada del encabezado Referer.
     * Se descarta el host a propósito para que nadie pueda usar esto como redirección a otro sitio.
     */
    public String rutaDeRetorno(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI uri = URI.create(referer.trim());
            String path = uri.getRawPath();
            if (path == null || !path.startsWith("/") || path.startsWith("//") || path.contains("\\")) {
                return "/";
            }
            String query = uri.getRawQuery();
            return query == null ? path : path + "?" + query;
        } catch (IllegalArgumentException e) {
            return "/";
        }
    }
}
