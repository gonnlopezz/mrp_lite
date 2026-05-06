package unpsjb.labprog.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanningRequestDTO {
    private String workshopCode;
    private String productName;
    private LocalDateTime startDate;

    
}