package unpsjb.labprog.backend.model;


import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Periodo {
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private int duracion;
    
    public Periodo() {
    }
    
    public Periodo(LocalDateTime inicio, LocalDateTime fin, int duracion) {
        this.inicio = inicio;
        this.fin = fin;
        this.duracion = duracion;
    }
}
