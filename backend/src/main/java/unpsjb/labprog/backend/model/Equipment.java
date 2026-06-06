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
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private int capacity;

    @ManyToOne
    private EquipmentType type;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    @OrderBy("period.endDate DESC")
    private List<Planning> plannings = new ArrayList<>();

    public LocalDateTime firstAvailableSlotAfter(LocalDateTime requestedTime) {
        if (this.plannings == null || this.plannings.isEmpty()) 
            return requestedTime;
        
        LocalDateTime result = requestedTime;
        
        for(Planning p : this.plannings) {
            if (p.getPeriod().getEndDate().isAfter(result)) {
                result = p.getPeriod().getEndDate();
            }
        }
        return result;
    }

}
