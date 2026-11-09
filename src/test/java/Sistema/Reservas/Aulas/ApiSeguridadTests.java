package Sistema.Reservas.Aulas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Con api.publica=false toda la API exige HTTP Basic. */
@SpringBootTest(properties = "api.publica=false")
@AutoConfigureMockMvc
class ApiSeguridadTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiPrivadaSinCredencialesDevuelve401() throws Exception {
        for (String ruta : new String[]{"/api/aulas", "/api/cursos", "/api/usuarios", "/api/reservas"}) {
            mockMvc.perform(get(ruta)).andExpect(status().isUnauthorized());
        }
    }
}
