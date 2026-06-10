package unpsjb.labprog.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private int tiempo;

    @ManyToOne
    private TipoEquipo tipo;

    public long calculateDurationFor(Equipo aEquipment) {
        return (long) Math.ceil((double) this.getTiempo() / aEquipment.getCapacidad());
    }
}
