package unpsjb.labprog.backend.business.equipment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.EquipmentType;

@Repository
public interface EquipmentTypeRepository extends CrudRepository<EquipmentType, Integer>, PagingAndSortingRepository<EquipmentType, Integer> {
    
}
