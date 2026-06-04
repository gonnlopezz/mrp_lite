package unpsjb.labprog.backend.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unpsjb.labprog.backend.exception.BusinessException;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ManufacturingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    private OrderState state = OrderState.PENDIENTE;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Product product;


    // Métodos

    public void markAsPlanned() {
        this.state = OrderState.PLANIFICADO;
    }

    public void markAsUnschedulable() {
        this.state = OrderState.NO_PLANIFICABLE;
    }

    public void validatePlannable() {
        if (this.state == OrderState.PLANIFICADO) 
            throw new BusinessException("El pedido ya se encuentra en estado planificado");
        if (this.state == OrderState.NO_PLANIFICABLE) 
            throw new BusinessException("El pedido ya se encuentra en estado no planificable");
    }
}
