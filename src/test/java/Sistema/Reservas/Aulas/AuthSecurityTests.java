package Sistema.Reservas.Aulas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void paginaProtegidaRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/reservas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginYRegistroSonPublicos() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/registro")).andExpect(status().isOk());
    }

    @Test
    void registroYLuegoLogin() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Prueba Uno")
                        .param("correo", "prueba.uno@etitc.edu.co")
                        .param("rol", "DOCENTE")
                        .param("password", "Clave1234")
                        .param("confirmarPassword", "Clave1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(formLogin("/login").user("correo", "prueba.uno@etitc.edu.co").password("password", "Clave1234"))
                .andExpect(authenticated());

        mockMvc.perform(formLogin("/login").user("correo", "prueba.uno@etitc.edu.co").password("password", "otraClave9"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void registroConContrasenasDistintasNoCreaLaCuenta() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Prueba Dos")
                        .param("correo", "prueba.dos@etitc.edu.co")
                        .param("rol", "ESTUDIANTE")
                        .param("password", "Clave1234")
                        .param("confirmarPassword", "Distinta1234"))
                .andExpect(status().isOk());

        mockMvc.perform(formLogin("/login").user("correo", "prueba.dos@etitc.edu.co").password("password", "Clave1234"))
                .andExpect(unauthenticated());
    }

    @Test
    void registroConContrasenaDebilNoCreaLaCuenta() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Prueba Tres")
                        .param("correo", "prueba.tres@etitc.edu.co")
                        .param("rol", "DOCENTE")
                        .param("password", "soloLetras")      // sin número
                        .param("confirmarPassword", "soloLetras"))
                .andExpect(status().isOk());

        mockMvc.perform(formLogin("/login").user("correo", "prueba.tres@etitc.edu.co").password("password", "soloLetras"))
                .andExpect(unauthenticated());
    }

    @Test
    void usuarioExistenteSinContrasenaPuedeCrearlaYNoSePuedeRepetir() throws Exception {
        // Ya no hay usuarios sembrados: se crea uno por la API (sin contraseña), como se haría desde Postman
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Carlos Pérez\",\"correo\":\"carlos.perez@etitc.edu.co\",\"rol\":\"DOCENTE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Carlos Pérez")
                        .param("correo", "carlos.perez@etitc.edu.co")
                        .param("rol", "DOCENTE")
                        .param("password", "Clave1234")
                        .param("confirmarPassword", "Clave1234"))
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(formLogin("/login").user("correo", "carlos.perez@etitc.edu.co").password("password", "Clave1234"))
                .andExpect(authenticated());

        // Un segundo intento con el mismo correo ya no es posible
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Otra Persona")
                        .param("correo", "carlos.perez@etitc.edu.co")
                        .param("rol", "DOCENTE")
                        .param("password", "Otra12345")
                        .param("confirmarPassword", "Otra12345"))
                .andExpect(status().isOk());
    }

    @Test
    void elLoginSiempreTerminaEnElMenuPrincipal() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                .param("nombre", "Prueba Menu")
                .param("correo", "prueba.menu@etitc.edu.co")
                .param("rol", "DOCENTE")
                .param("password", "Clave1234")
                .param("confirmarPassword", "Clave1234"));

        mockMvc.perform(formLogin("/login").user("correo", "prueba.menu@etitc.edu.co").password("password", "Clave1234"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));
    }
}
