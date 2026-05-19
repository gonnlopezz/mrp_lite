package unpsjb.labprog.backend.business.order;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.ManufacturingOrder;

@Repository
public interface OrderRepository extends CrudRepository<ManufacturingOrder, Long>, PagingAndSortingRepository<ManufacturingOrder, Long> {
    
}
