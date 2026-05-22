package unpsjb.labprog.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import unpsjb.labprog.backend.model.ManufacturingOrder;

@Getter
@Setter
public class PlanningFromOrderRequestDTO {
    private ManufacturingOrder order;
    private LocalDateTime startDate;
}