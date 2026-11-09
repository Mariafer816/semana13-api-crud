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

/** Pruebas de /api/cursos, /api/usuarios y /api/reservas (API pública por defecto). */
@SpringBootTest
@AutoConfigureMockMvc
class RecursosApiTests {

    @Autowired
    private MockMvc mockMvc;

    private long idDe(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private MvcResult enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b,
                             String json, int esperado) throws Exception {
        if (json != null) {
            b.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(b).andExpect(status().is(esperado)).andReturn();
    }

    // ---------- CURSOS ----------
    @Test
    void cursosCrud() throws Exception {
        mockMvc.perform(get("/api/cursos")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());

        long id = idDe(enviar(post("/api/cursos"), "{\"nombre\":\"Curso API\",\"horas\":40}", 200));
        mockMvc.perform(get("/api/cursos/" + id)).andExpect(jsonPath("$.nombre").value("Curso API"));
        mockMvc.perform(put("/api/cursos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Curso API 2\",\"horas\":60}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.horas").value(60));
        mockMvc.perform(delete("/api/cursos/" + id)).andExpect(status().isOk());
        mockMvc.perform(get("/api/cursos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void cursosErrores() throws Exception {
        enviar(post("/api/cursos"), "{\"nombre\":\"\",\"horas\":0}", 400);
        enviar(get("/api/cursos/999999"), null, 404);
        enviar(put("/api/cursos/999999"), "{\"nombre\":\"X\",\"horas\":5}", 404);
        enviar(delete("/api/cursos/999999"), null, 404);
    }

    // ---------- USUARIOS ----------
    @Test
    void usuariosCrudYReglas() throws Exception {
        String json = "{\"nombre\":\"Usuario API\",\"correo\":\"usuario.api@etitc.edu.co\",\"rol\":\"DOCENTE\"}";
        long id = idDe(enviar(post("/api/usuarios"), json, 200));

        mockMvc.perform(get("/api/usuarios/" + id))
                .andExpect(jsonPath("$.correo").value("usuario.api@etitc.edu.co"))
                .andExpect(jsonPath("$.password").doesNotExist());

        enviar(post("/api/usuarios"), json, 400);   // correo repetido
        enviar(post("/api/usuarios"),
                "{\"nombre\":\"X\",\"correo\":\"x@etitc.edu.co\",\"rol\":\"INVENTADO\"}", 400);
        enviar(post("/api/usuarios"), "{\"nombre\":\"\",\"correo\":\"mal\",\"rol\":\"\"}", 400);

        mockMvc.perform(put("/api/usuarios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Usuario API 2\",\"correo\":\"usuario.api@etitc.edu.co\",\"rol\":\"ESTUDIANTE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rol").value("ESTUDIANTE"));

        enviar(delete("/api/usuarios/" + id), null, 200);
        enviar(get("/api/usuarios/" + id), null, 404);
        enviar(put("/api/usuarios/999999"), json, 404);
    }

    // ---------- RESERVAS ----------
    @Test
    void reservasCrudYReglas() throws Exception {
        long aula = idDe(enviar(post("/api/aulas"),
                "{\"nombre\":\"R-1\",\"capacidad\":20,\"ubicacion\":\"Edificio R\",\"tipo\":\"Laboratorio\"}", 200));
        long usuario = idDe(enviar(post("/api/usuarios"),
                "{\"nombre\":\"Reservante\",\"correo\":\"reservante@etitc.edu.co\",\"rol\":\"DOCENTE\"}", 200));
        long curso = idDe(enviar(post("/api/cursos"), "{\"nombre\":\"Curso R\",\"horas\":10}", 200));

        String cuerpo = "{\"aulaId\":%d,\"usuarioId\":%d,\"cursoId\":%d,\"fecha\":\"2099-12-15\",\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}"
                .formatted(aula, usuario, curso);
        long id = idDe(enviar(post("/api/reservas"), cuerpo, 200));

        mockMvc.perform(get("/api/reservas/" + id))
                .andExpect(jsonPath("$.aula.id").value(aula));
        mockMvc.perform(get("/api/reservas/fecha/2099-12-15")).andExpect(status().isOk());
        mockMvc.perform(get("/api/reservas/aula/" + aula)).andExpect(jsonPath("$[0].id").value(id));

        // cruce de horario -> 400
        enviar(post("/api/reservas"), cuerpo.replace("08:00", "09:00").replace("10:00", "11:00"), 400);
        // hora fin <= inicio -> 400
        enviar(post("/api/reservas"), cuerpo.replace("\"horaFin\":\"10:00\"", "\"horaFin\":\"07:00\""), 400);
        // aula inexistente -> 400
        enviar(post("/api/reservas"), cuerpo.replace("\"aulaId\":" + aula, "\"aulaId\":999999"), 400);
        // fecha con formato incorrecto -> 400
        enviar(get("/api/reservas/fecha/hoy"), null, 400);

        mockMvc.perform(put("/api/reservas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.replace("08:00", "13:00").replace("10:00", "15:00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.horaInicio").value("13:00:00"));

        // el aula, el usuario y el curso tienen reservas -> no se pueden eliminar (400)
        enviar(delete("/api/aulas/" + aula), null, 400);
        enviar(delete("/api/usuarios/" + usuario), null, 400);
        enviar(delete("/api/cursos/" + curso), null, 400);

        enviar(delete("/api/reservas/" + id), null, 200);
        enviar(get("/api/reservas/" + id), null, 404);
        enviar(put("/api/reservas/999999"), cuerpo, 404);
        enviar(delete("/api/reservas/999999"), null, 404);
    }
}
