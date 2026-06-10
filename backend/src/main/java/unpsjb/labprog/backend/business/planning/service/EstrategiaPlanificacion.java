package unpsjb.labprog.backend.business.planning.service;


import java.time.LocalDateTime;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Product;
import unpsjb.labprog.backend.model.Workshop;

public interface EstrategiaPlanificacion {
    PlanningProcess ejecutar(Product producto, Workshop taller, AgendaTaller agenda, LocalDateTime fechaReferencia);
}