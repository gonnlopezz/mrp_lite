package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.model.*;

@Component("FORWARD")
public class PlanificacionForward implements EstrategiaPlanificacion {

    @Override
    public PlanningProcess ejecutar(Product producto, Workshop taller, AgendaTaller agenda, LocalDateTime inicio) {
        List<Planning> planificaciones = new ArrayList<>();
        LocalDateTime tiempoActual = inicio;

        for (Task tarea : producto.getTasks()) {
            Equipment equipo = taller.findEquipmentForType(tarea.getType());
            
            LocalDateTime inicioDisponible = equipo.firstAvailableSlotAfter(tiempoActual);
            long duracion = tarea.calculateDurationFor(equipo);
            LocalDateTime fin = inicioDisponible.plusMinutes(duracion);
            
            Period periodo = new Period(inicioDisponible, fin, tarea.getDuration());
            planificaciones.add(new Planning(tarea, equipo, periodo));
            
            tiempoActual = fin;
        }

        return new PlanningProcess(planificaciones, planificaciones.get(0).getPeriod().getStart(), tiempoActual);
    }
}