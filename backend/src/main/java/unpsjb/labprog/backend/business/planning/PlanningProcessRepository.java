package unpsjb.labprog.backend.business.planning;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.PlanningProcess;

@Repository
public interface PlanningProcessRepository extends CrudRepository<PlanningProcess, Long> {
    
}
