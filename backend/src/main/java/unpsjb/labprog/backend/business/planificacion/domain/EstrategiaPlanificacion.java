package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Producto;
import unpsjb.labprog.backend.model.Taller;

public interface EstrategiaPlanificacion {
    ProcesoPlanificacion planificar(Producto producto, Taller taller, Agenda agenda, LocalDateTime fechaReferencia);
}