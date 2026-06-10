package unpsjb.labprog.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String código;

    @Column(nullable = false)
    private int capacidad;

    @ManyToOne
    private TipoEquipo tipo;

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY)
    @OrderBy("periodo.fin DESC")
    private List<Planificacion> planificaciones = new ArrayList<>();

    public LocalDateTime firstAvailableSlotAfter(LocalDateTime requestedTime) {
        if (this.planificaciones == null || this.planificaciones.isEmpty()) 
            return requestedTime;
        
        LocalDateTime result = requestedTime;
        
        for(Planificacion p : this.planificaciones) {
            if (p.getPeriodo().getFin().isAfter(result)) {
                result = p.getPeriodo().getFin();
            }
        }
        return result;
    }

}
