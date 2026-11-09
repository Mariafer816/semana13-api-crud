package Sistema.Reservas.Aulas.Model;

public class CursoDto {
    private Long id;
    private String nombre;
    private int horas;

    public CursoDto() {}

    public CursoDto(Long id, String nombre, int horas) {
        this.id = id;
        this.nombre = nombre;
        this.horas = horas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getHoras() { return horas; }
    public void setHoras(int horas) { this.horas = horas; }
}