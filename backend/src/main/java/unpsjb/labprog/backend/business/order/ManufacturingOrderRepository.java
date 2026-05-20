package unpsjb.labprog.backend.business.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.ManufacturingOrder;

@Repository
public interface ManufacturingOrderRepository extends CrudRepository<ManufacturingOrder, Long>, PagingAndSortingRepository<ManufacturingOrder, Long> {
    @Query("SELECT o FROM ManufacturingOrder o " +
            "Where o.customer.companyName ILIKE CONCAT('%', :term, '%') " +
            "OR o.product.name ILIKE CONCAT('%', :term, '%')")
    Page<ManufacturingOrder> search(String term, Pageable pageable);
}
