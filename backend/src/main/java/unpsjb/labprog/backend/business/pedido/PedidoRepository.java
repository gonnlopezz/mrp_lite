package unpsjb.labprog.backend.business.pedido;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.EstadoPedido;

@Repository
public interface PedidoRepository
                extends JpaRepository<Pedido, Long> {
        @Query("SELECT o FROM Pedido o " +
                        "Where o.cliente.razonSocial ILIKE CONCAT('%', :term, '%') " +
                        "OR o.producto.nombre ILIKE CONCAT('%', :term, '%')")
        public Page<Pedido> search(String term, Pageable pageable);

        @Query("SELECT o FROM Pedido o " +
                        "WHERE o.cliente.cuit = :cuit AND o.fechaEntrega = :deliveryDate")
        public Pedido findByCustomerCuitAndDeliveryDate(long cuit, LocalDate deliveryDate);

        List<Pedido> findByEstadoOrderByFechaEntregaAsc(EstadoPedido estado);

        boolean existsByClienteId(int clienteId);

        boolean existsByProductoId(int productoId);

        Page<Pedido> findByEstadoOrderByFechaEntregaAsc(EstadoPedido estado, Pageable pageable);

        @Query("SELECT o FROM Pedido o ORDER BY " +
                        "CASE WHEN o.estado = unpsjb.labprog.backend.model.EstadoPedido.PENDIENTE       THEN 1 " +
                        "     WHEN o.estado = unpsjb.labprog.backend.model.EstadoPedido.NO_PLANIFICABLE THEN 2 " +
                        "     WHEN o.estado = unpsjb.labprog.backend.model.EstadoPedido.PLANIFICADO     THEN 3 " +
                        "     ELSE 4 END ASC, " +
                        "o.fechaEntrega ASC")
        Page<Pedido> findAllOrderedByStatePriority(Pageable pageable);

}


