package unpsjb.labprog.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanAllPendingRequestDTO {
    private LocalDateTime startDate;
}