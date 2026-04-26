package unpsjb.labprog.backend.business.equipment;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.EquipmentType;

@Repository
public interface EquipmentTypeRepository extends CrudRepository<EquipmentType, Integer>, PagingAndSortingRepository<EquipmentType, Integer> {
    @Query("Select e FROM EquipmentType e Where e.name = ?1")
    Optional<EquipmentType> findByCode(String name);
}
