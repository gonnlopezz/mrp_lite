package unpsjb.labprog.backend.business.workshop;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Workshop;

@Repository
public interface WorkshopRepository extends CrudRepository<Workshop, Integer>, PagingAndSortingRepository<Workshop, Integer>   {
    @Query("Select e FROM Workshop e Where e.code = ?1")
    Optional<Workshop> findByCode(String code);
}
