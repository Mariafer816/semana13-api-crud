package Sistema.Reservas.Aulas;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de /api/aulas (API pública por defecto: api.publica=true). */
@SpringBootTest
@AutoConfigureMockMvc
class AulaApiTests {

    private static final String JSON_VALIDO = """
            {"nombre":"API-101","capacidad":30,"ubicacion":"Edificio API","tipo":"Laboratorio","disponible":true}""";

    @Autowired
    private MockMvc mockMvc;

    private long crearAula() throws Exception {
        MvcResult r = mockMvc.perform(post("/api/aulas")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andReturn();
        Number id = JsonPath.read(r.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void listarDevuelve200ConArregloJson() throws Exception {
        mockMvc.perform(get("/api/aulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void cicloCompletoCrearConsultarActualizarEliminar() throws Exception {
        long id = crearAula();

        mockMvc.perform(get("/api/aulas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("API-101"));

        mockMvc.perform(put("/api/aulas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"API-101B","capacidad":45,"ubicacion":"Edificio API 2","tipo":"Auditorio","disponible":false}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("API-101B"))
                .andExpect(jsonPath("$.tipo").value("Auditorio"))
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(delete("/api/aulas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(200));

        mockMvc.perform(get("/api/aulas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void postConDatosInvalidosDevuelve400ConDetalle() throws Exception {
        mockMvc.perform(post("/api/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","capacidad":0,"ubicacion":"","tipo":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.capacidad").exists());
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/aulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"X\", \"capacidad\": \"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/aulas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void idInexistenteDevuelve404EnGetPutDelete() throws Exception {
        mockMvc.perform(get("/api/aulas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));

        mockMvc.perform(put("/api/aulas/999999")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/aulas/999999")).andExpect(status().isNotFound());
    }
}
