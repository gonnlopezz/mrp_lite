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
public class Planning {

    public Planning(Task aTask, Equipment aEquipment, Period aPeriod) {
        this.task = aTask;
        this.equipment = aEquipment;
        this.period = aPeriod;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Period period;

    @ManyToOne
    private Task task;

    @ManyToOne
    @JsonIgnoreProperties("plannings")
    private Equipment equipment;
}
