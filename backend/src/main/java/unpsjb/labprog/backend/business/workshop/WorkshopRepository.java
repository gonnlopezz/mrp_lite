package unpsjb.labprog.backend.business.workshop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.Workshop;

@Repository
public interface WorkshopRepository
                extends CrudRepository<Workshop, Integer>, PagingAndSortingRepository<Workshop, Integer> {
        @Query("Select e FROM Workshop e Where e.code = ?1")
        Optional<Workshop> findByCode(String code);

        @Query("SELECT w FROM Workshop w " +
                        "JOIN w.equipments e " +
                        "WHERE e.type IN :types " +
                        "GROUP BY w.id " +
                        "HAVING COUNT(DISTINCT e.type) = :count " +
                        "ORDER BY w.code ASC " +
                        "LIMIT 1")
        Optional<Workshop> findByEquipmentTypes(List<EquipmentType> types, int count);

        @Query("SELECT w FROM Workshop w " +
                        "JOIN w.equipments e " +
                        "WHERE e.type IN :types " +
                        "GROUP BY w.id " +
                        "HAVING COUNT(DISTINCT e.type) = :count " +
                        "ORDER BY w.code ASC")
        List<Workshop> findAllByEquipmentTypes(List<EquipmentType> types, int count);

        @Query("SELECT w FROM Workshop w " +
                        "WHERE w.code ILIKE CONCAT('%', :term, '%') " +
                        "OR w.name ILIKE CONCAT('%', :term, '%')")
        Page<Workshop> search(String term, Pageable pageable);

}
