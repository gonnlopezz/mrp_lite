package unpsjb.labprog.backend.business.workshop;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Workshop;

@Repository
public interface WorkshopRepository extends CrudRepository<Workshop, Long> {
    
}
