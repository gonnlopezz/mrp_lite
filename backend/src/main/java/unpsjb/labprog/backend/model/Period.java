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
    private LocalDateTime end;
    private int duration;
    
    public Period(LocalDateTime start, LocalDateTime end, int duration) {
        this.start = start;
        this.end = end;
        this.duration = duration;
    }
}
