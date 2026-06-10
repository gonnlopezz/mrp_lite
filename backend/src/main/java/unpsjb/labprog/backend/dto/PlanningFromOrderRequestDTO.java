package unpsjb.labprog.backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import unpsjb.labprog.backend.model.Pedido;

@Getter
@Setter
public class PlanningFromOrderRequestDTO {
    private Pedido order;
    private LocalDateTime startDate;
}