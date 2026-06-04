package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.PlanningProcess;

@Repository
public interface PlanningProcessRepository
                extends JpaRepository<PlanningProcess, Long> {

        @Query("SELECT MAX(p.period.endDate) FROM Planning p WHERE p.equipment.id = :equipmentId")
        public Optional<LocalDateTime> findMaxEndTimeForEquipment(@Param("equipmentId") Long equipmentId);

        @Query("SELECT pp FROM PlanningProcess pp " +
                        "JOIN FETCH pp.plannings p " +
                        "JOIN FETCH p.equipment e " +
                        "WHERE e IN (SELECT eq FROM Workshop w JOIN w.equipments eq WHERE w.id = :workshopId)")
        public List<PlanningProcess> findAllByWorkshopId(@Param("workshopId") Integer workshopId);

        List<PlanningProcess> findByOrderId(Long orderId);

        @Query("SELECT DISTINCT pp FROM PlanningProcess pp " +
                        "JOIN FETCH pp.plannings pl " +
                        "JOIN FETCH pl.equipment eq " +
                        "LEFT JOIN pp.order o " +
                        "WHERE (:workshopId IS NULL OR eq IN (SELECT e FROM Workshop w JOIN w.equipments e WHERE w.id = :workshopId)) "
                        +
                        "AND (:orderId IS NULL OR o.id = :orderId) " +
                        "ORDER BY pp.id ASC")
        List<PlanningProcess> findProcessesByFilters(
                        @Param("workshopId") Long workshopId,
                        @Param("orderId") Long orderId);

}
