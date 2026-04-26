package unpsjb.labprog.backend.business.equipment;

import org.springframework.data.repository.CrudRepository;

import unpsjb.labprog.backend.model.Equipment;

public interface EquipmentRepository extends CrudRepository<Equipment, Integer> {
    
}
