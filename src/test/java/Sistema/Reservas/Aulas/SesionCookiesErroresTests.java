package Sistema.Reservas.Aulas;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SesionCookiesErroresTests {

    @Autowired
    private MockMvc mockMvc;

    // ---------- HttpSession ----------

    @Test
    void sesionGuardaElNombreAlIngresarYLoDestruyeAlSalir() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                .param("nombre", "Prueba Sesion")
                .param("correo", "prueba.sesion@etitc.edu.co")
                .param("rol", "DOCENTE")
                .param("password", "Clave1234")
                .param("confirmarPassword", "Clave1234"));

        MvcResult login = mockMvc.perform(formLogin("/login")
                        .user("correo", "prueba.sesion@etitc.edu.co").password("password", "Clave1234"))
                .andExpect(authenticated())
                .andReturn();

        MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);
        assertEquals("Prueba Sesion", sesion.getAttribute("usuarioNombre"));

        mockMvc.perform(post("/logout").with(csrf()).session(sesion))
                .andExpect(redirectedUrl("/login?logout"));

        assertTrue(sesion.isInvalid(), "La HttpSession debe quedar invalidada al cerrar sesión");
    }

    @Test
    void registrarseAbreLaSesionSinPasarPorElLogin() throws Exception {
        MvcResult registro = mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Prueba Directa")
                        .param("correo", "prueba.directa@etitc.edu.co")
                        .param("rol", "ESTUDIANTE")
                        .param("password", "Clave1234")
                        .param("confirmarPassword", "Clave1234"))
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession sesion = (MockHttpSession) registro.getRequest().getSession(false);
        assertEquals("Prueba Directa", sesion.getAttribute("usuarioNombre"));

        // Con esa misma sesión ya se puede entrar a páginas protegidas y se ve el nombre en el menú
        mockMvc.perform(get("/").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prueba Directa")));
    }

    // ---------- Cookie de tema ----------

    @Test
    void cambiarTemaGuardaCookieYVuelveALaPaginaAnterior() throws Exception {
        mockMvc.perform(post("/preferencias/tema").with(csrf()).with(user("a@b.com"))
                        .param("valor", "oscuro")
                        .header("Referer", "http://localhost/aulas?page=1"))
                .andExpect(redirectedUrl("/aulas?page=1"))
                .andExpect(header().string("Set-Cookie", containsString("tema=oscuro")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/")));
    }

    @Test
    void temaInvalidoSeConvierteEnClaroYRefererMaliciosoNoRedirigeFuera() throws Exception {
        mockMvc.perform(post("/preferencias/tema").with(csrf()).with(user("a@b.com"))
                        .param("valor", "<script>")
                        .header("Referer", "http://localhost//evil.com"))
                .andExpect(redirectedUrl("/"))
                .andExpect(header().string("Set-Cookie", containsString("tema=claro")));
    }

    @Test
    void conCookieOscuraLaPaginaCargaElTemaOscuroYMuestraAlUsuarioActivo() throws Exception {
        mockMvc.perform(get("/aulas").with(user("a@b.com")).cookie(new Cookie("tema", "oscuro")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tema-oscuro.css")))
                .andExpect(content().string(containsString("a@b.com")));
    }

    @Test
    void sinCookieSeUsaElTemaClaro() throws Exception {
        mockMvc.perform(get("/aulas").with(user("a@b.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("tema-oscuro.css"))));
    }

    // ---------- Páginas de error ----------

    @Test
    void paginaPersonalizada404() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página no encontrada")))
                .andExpect(content().string(containsString("Volver a la aplicación")));
    }

    @Test
    void paginaPersonalizada500() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error interno del servidor")))
                .andExpect(content().string(containsString("Volver a la aplicación")));
    }
}
