package unpsjb.labprog.backend.business.customer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer>, PagingAndSortingRepository<Customer, Integer> {
    
}
