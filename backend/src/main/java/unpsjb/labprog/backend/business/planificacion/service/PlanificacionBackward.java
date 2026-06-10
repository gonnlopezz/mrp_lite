package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Component("BACKWARD")
public class PlanificacionBackward implements EstrategiaPlanificacion {

    @Override
    public ProcesoPlanificacion ejecutar(Producto producto, Taller taller, AgendaTaller agenda, LocalDateTime deadline) {
        LinkedList<Planificacion> planificaciones = new LinkedList<>();
        LocalDateTime finActual = deadline;

        for (Tarea tarea : producto.getReverseTasks()) {
            Equipo equipo = taller.findEquipmentForType(tarea.getTipo());
            AgendaEquipo agendaEquipo = agenda.agendaDe(equipo);
            long duracion = tarea.calculateDurationFor(equipo);

            LocalDateTime fin = agendaEquipo.encontrarFinBackward(finActual, duracion);
            
            if (fin == null)  throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido", planificaciones.size());


            LocalDateTime inicio = fin.minusMinutes(duracion);
            Periodo periodo = new Periodo(inicio, fin, tarea.getTiempo());

            planificaciones.addFirst(new Planificacion(tarea, equipo, periodo));
            agendaEquipo.ocupar(periodo);
            
            finActual = inicio;
        }

        return new ProcesoPlanificacion(new ArrayList<>(planificaciones), planificaciones.getFirst().getPeriodo().getInicio(), deadline);
    }
}