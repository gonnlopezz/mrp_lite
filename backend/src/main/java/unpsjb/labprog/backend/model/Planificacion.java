package unpsjb.labprog.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Planificacion {

    public Planificacion(Tarea aTarea, Equipo aEquipo, Periodo aPeriodo) {
        this.tarea = aTarea;
        this.equipo = aEquipo;
        this.periodo = aPeriodo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Periodo periodo;

    @ManyToOne
    private Tarea tarea;

    @ManyToOne
    @JsonIgnoreProperties("planificaciones")
    private Equipo equipo;
}
