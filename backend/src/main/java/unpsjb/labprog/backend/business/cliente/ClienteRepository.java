package unpsjb.labprog.backend.business.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Cliente;

@Repository
public interface ClienteRepository
        extends CrudRepository<Cliente, Integer>, PagingAndSortingRepository<Cliente, Integer> {
    @Query("SELECT c FROM Cliente c WHERE c.cuit = :cuit")
    public Cliente findByCuit(String cuit);

    @Query("SELECT c FROM Cliente c" + 
    " WHERE c.razónSocial ILIKE CONCAT('%', :term, '%')" +
     "OR CAST(c.cuit AS string) ILIKE CONCAT('%', :term, '%') ORDER BY c.razónSocial ASC")
    public Page<Cliente> search(String term, Pageable pageable);

}