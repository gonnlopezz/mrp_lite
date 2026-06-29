package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.TipoEstrategia;
import unpsjb.labprog.backend.model.*;

@Component(TipoEstrategia.FORWARD)
public class PlanificacionForward extends EstrategiaPlanificacionBase {
    @Override
    protected List<Tarea> obtenerTareas(Producto producto) {
        return producto.getTareas();
    }

    @Override
    protected Periodo ocuparHueco(Agenda agenda, Tarea tarea, Equipo equipo, LocalDateTime cursor) {
        return agenda.ocuparEspacioForward(tarea, equipo, cursor);
    }

    @Override
    protected void agregarPlanificacion(LinkedList<Planificacion> planificaciones, Planificacion planificacion) {
        planificaciones.addLast(planificacion);
    }

    @Override
    protected LocalDateTime avanzarCursor(Periodo periodo) {
        return periodo.getFin();
    }

    @Override
    protected LocalDateTime calcularFin(LocalDateTime fechaReferencia, LocalDateTime cursor) {
        return cursor;
    }

}