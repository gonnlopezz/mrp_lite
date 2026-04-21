package unpsjb.labprog.backend.business.equipment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.EquipmentType;

@Repository
public interface EquipmentTypeRepository extends CrudRepository<EquipmentType, Integer> {
    
}
