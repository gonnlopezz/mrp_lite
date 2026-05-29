package unpsjb.labprog.backend.business.order;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.OrderState;

@Repository
public interface ManufacturingOrderRepository extends CrudRepository<ManufacturingOrder, Long>, PagingAndSortingRepository<ManufacturingOrder, Long> {
    @Query("SELECT o FROM ManufacturingOrder o " +
            "Where o.customer.companyName ILIKE CONCAT('%', :term, '%') " +
            "OR o.product.name ILIKE CONCAT('%', :term, '%')")
    public Page<ManufacturingOrder> search(String term, Pageable pageable);

    @Query("SELECT o FROM ManufacturingOrder o " +
            "WHERE o.customer.cuit = :cuit AND o.deliveryDate = :deliveryDate")
    public ManufacturingOrder findByCustomerCuitAndDeliveryDate(long cuit, LocalDate deliveryDate);

    List<ManufacturingOrder> findByStateOrderByDeliveryDateAsc(OrderState state);
}
