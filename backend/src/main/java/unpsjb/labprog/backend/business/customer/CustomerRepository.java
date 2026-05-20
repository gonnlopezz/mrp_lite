package unpsjb.labprog.backend.business.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Customer;

@Repository
public interface CustomerRepository
        extends CrudRepository<Customer, Integer>, PagingAndSortingRepository<Customer, Integer> {
    @Query("SELECT c FROM Customer c WHERE c.cuit = :cuit")
    public Customer findByCuit(String cuit);

    @Query("SELECT c FROM Customer c" + 
    " WHERE c.companyName ILIKE CONCAT('%', :term, '%')" +
     "OR CAST(c.cuit AS string) ILIKE CONCAT('%', :term, '%') ORDER BY c.companyName ASC")
    public Page<Customer> search(String term, Pageable pageable);

}