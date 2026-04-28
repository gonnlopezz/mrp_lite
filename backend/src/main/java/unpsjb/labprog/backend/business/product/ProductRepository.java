package unpsjb.labprog.backend.business.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


import unpsjb.labprog.backend.model.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {
    @Query("Select e FROM Product e Where e.name = ?1")
    Optional<Product> findByName(String name);
}
