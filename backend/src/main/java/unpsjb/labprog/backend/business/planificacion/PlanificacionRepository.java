package unpsjb.labprog.backend.business.planificacion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@Repository
public interface PlanificacionRepository
                extends JpaRepository<ProcesoPlanificacion, Long> {

        @Query("SELECT pp FROM ProcesoPlanificacion pp " +
                        "JOIN FETCH pp.planificaciones p " +
                        "JOIN FETCH p.equipo e " +
                        "WHERE e IN (SELECT eq FROM Taller w JOIN w.equipos eq WHERE w.id = :workshopId)")
        public List<ProcesoPlanificacion> findAllByWorkshopId(@Param("workshopId") Integer workshopId);

        List<ProcesoPlanificacion> findByPedidoId(Long orderId);

        @Query("SELECT DISTINCT pp FROM ProcesoPlanificacion pp " +
                        "JOIN FETCH pp.planificaciones pl " +
                        "JOIN FETCH pl.equipo eq " +
                        "LEFT JOIN pp.pedido o " +
                        "WHERE (:workshopId IS NULL OR eq IN (SELECT e FROM Taller w JOIN w.equipos e WHERE w.id = :workshopId)) "
                        +
                        "AND (:orderId IS NULL OR o.id = :orderId) " +
                        "ORDER BY pp.id ASC")
        List<ProcesoPlanificacion> findProcessesByFilters(
                        @Param("workshopId") Long workshopId,
                        @Param("orderId") Long orderId);

        @Query("SELECT p FROM Planificacion p " +
                        "JOIN FETCH p.equipo e " +
                        "WHERE e IN (SELECT eq FROM Taller w JOIN w.equipos eq WHERE w.id = :tallerId) " +
                        "ORDER BY e.id ASC, p.periodo.inicio ASC")
        List<Planificacion> planificacionesPorTaller(@Param("tallerId") Long tallerId);

        @Query("SELECT p FROM Planificacion p " +
                        "JOIN FETCH p.equipo e " +
                        "WHERE p.periodo.fin >= :fechaInicio " +
                        "ORDER BY e.id ASC, p.periodo.inicio ASC")
        List<Planificacion> todasPlanificacionesOrdenadas(@Param("fechaInicio") LocalDateTime fechaInicio);
}
