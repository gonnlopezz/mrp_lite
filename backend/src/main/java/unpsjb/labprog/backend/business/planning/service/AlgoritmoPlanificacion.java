package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Service
public class AlgoritmoPlanificacion {

    public PlanningProcess planificarBackward(
            Product producto, Workshop taller, AgendaTaller agenda, LocalDateTime deadline) {
        
        LinkedList<Planning> planificaciones = new LinkedList<>();
        LocalDateTime finActual = deadline;

        for (Task tarea : producto.getReverseTasks()) {
            Equipment equipo = taller.findEquipmentForType(tarea.getType());
            AgendaEquipo agendaEquipo = agenda.agendaDe(equipo);
            long duracion = tarea.calculateDurationFor(equipo);

            LocalDateTime fin = agendaEquipo.encontrarFinBackward(finActual, duracion);
            
            if (fin == null) {
                throw new SchedulingException("Capacidad insuficiente en el equipamiento del taller.", planificaciones.size());
            }

            LocalDateTime inicio = fin.minusMinutes(duracion);
            Period periodo = new Period(inicio, fin, tarea.getDuration());

            planificaciones.addFirst(new Planning(tarea, equipo, periodo));
            
            // Registramos la ocupación fragmentando el hueco libre en la agenda
            agendaEquipo.ocupar(periodo);
            
            // Restricción de precedencia interna (Intra-unit constraint)
            finActual = inicio;
        }

        return new PlanningProcess(new ArrayList<>(planificaciones),
                planificaciones.getFirst().getPeriod().getStart(), deadline);
    }

   public PlanningProcess planificarForward(Product producto, Workshop taller, LocalDateTime inicio) {
        List<Planning> planificaciones = new ArrayList<>();
        LocalDateTime tiempoActual = inicio;

        for (Task tarea : producto.getTasks()) {
            Equipment equipo = taller.findEquipmentForType(tarea.getType());
            
            // Usás la lógica nativa de tu modelo: busca el primer hueco libre real en el equipo
            LocalDateTime inicioDisponible = equipo.firstAvailableSlotAfter(tiempoActual);
            long duracion = tarea.calculateDurationFor(equipo);
            LocalDateTime fin = inicioDisponible.plusMinutes(duracion);
            
            Period periodo = new Period(inicioDisponible, fin, tarea.getDuration());
            planificaciones.add(new Planning(tarea, equipo, periodo));
            
            // El tiempo actual avanza linealmente hacia el fin de esta tarea
            tiempoActual = fin;
        }

        return new PlanningProcess(planificaciones,
                planificaciones.get(0).getPeriod().getStart(), tiempoActual);
    }
}