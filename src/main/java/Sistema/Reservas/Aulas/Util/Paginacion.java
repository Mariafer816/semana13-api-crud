package Sistema.Reservas.Aulas.Util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;


public final class Paginacion {

    public static final int TAMANO_POR_DEFECTO = 5;
    public static final int TAMANO_MAXIMO = 50;

    private Paginacion() {
    }

    /** Quita espacios y convierte null en cadena vacía. */
    public static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public static int tamanoValido(int size) {
        if (size < 1) {
            return TAMANO_POR_DEFECTO;
        }
        return Math.min(size, TAMANO_MAXIMO);
    }

    public static <T> Page<T> consultar(int page, int size, Sort sort,
                                        Function<Pageable, Page<T>> consulta) {
        int pagina = Math.max(page, 0);
        int tamano = tamanoValido(size);

        Page<T> resultado = consulta.apply(PageRequest.of(pagina, tamano, sort));

        // Página fuera de rango: se vuelve a consultar la última página existente
        if (resultado.getTotalPages() > 0 && pagina >= resultado.getTotalPages()) {
            resultado = consulta.apply(PageRequest.of(resultado.getTotalPages() - 1, tamano, sort));
        }
        return resultado;
    }
}
