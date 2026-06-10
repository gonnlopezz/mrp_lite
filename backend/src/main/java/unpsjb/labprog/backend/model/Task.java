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
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int taskOrder;

    @Column(nullable = false)
    private int duration;

    @ManyToOne
    private EquipmentType type;

    public long calculateDurationFor(Equipment aEquipment) {
        return (long) Math.ceil((double) this.getDuration() / aEquipment.getCapacity());
    }
}
