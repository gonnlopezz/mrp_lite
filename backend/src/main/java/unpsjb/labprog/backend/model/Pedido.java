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
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDate fechaPedido;

    @Column(nullable = false)
    private LocalDate fechaEntrega;

    @Column(nullable = false)
    private int cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido", nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Producto producto;

    @Column
    private String motivoFalloPlanning;

    @Column
    private Integer cantidadPlanificable;

    // Métodos

    public void marcarComoPlanificado() {
        this.estado = EstadoPedido.PLANIFICADO;
    }

    public void marcarComoNoPlanificable(String razon, Integer cantidadPlanificable) {
        this.estado = EstadoPedido.NO_PLANIFICABLE;
        this.motivoFalloPlanning = razon;
        this.cantidadPlanificable = cantidadPlanificable;
    }

    public void validarPlanificable() {
        if (this.estado == EstadoPedido.PLANIFICADO)
            throw new BusinessException("El pedido ya se encuentra en estado planificado");
        if (this.estado == EstadoPedido.FINALIZADO)
            throw new BusinessException("El pedido ya se encuentra en estado finalizado");
    }
}
