package unpsjb.labprog.backend.model;


import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Period {
    private LocalDateTime start;
    private LocalDateTime endDate;
    private int duration;
    
    public Period() {
    }
    
    public Period(LocalDateTime start, LocalDateTime endDate, int duration) {
        this.start = start;
        this.endDate = endDate;
        this.duration = duration;
    }
}
