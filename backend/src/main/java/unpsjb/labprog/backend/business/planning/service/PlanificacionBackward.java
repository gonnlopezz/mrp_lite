package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Component("BACKWARD")
public class PlanificacionBackward implements EstrategiaPlanificacion {

    @Override
    public PlanningProcess ejecutar(Product producto, Workshop taller, AgendaTaller agenda, LocalDateTime deadline) {
        LinkedList<Planning> planificaciones = new LinkedList<>();
        LocalDateTime finActual = deadline;

        for (Task tarea : producto.getReverseTasks()) {
            Equipment equipo = taller.findEquipmentForType(tarea.getType());
            AgendaEquipo agendaEquipo = agenda.agendaDe(equipo);
            long duracion = tarea.calculateDurationFor(equipo);

            LocalDateTime fin = agendaEquipo.encontrarFinBackward(finActual, duracion);
            
            if (fin == null)  throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido", planificaciones.size());


            LocalDateTime inicio = fin.minusMinutes(duracion);
            Period periodo = new Period(inicio, fin, tarea.getDuration());

            planificaciones.addFirst(new Planning(tarea, equipo, periodo));
            agendaEquipo.ocupar(periodo);
            
            finActual = inicio;
        }

        return new PlanningProcess(new ArrayList<>(planificaciones), planificaciones.getFirst().getPeriod().getStart(), deadline);
    }
}