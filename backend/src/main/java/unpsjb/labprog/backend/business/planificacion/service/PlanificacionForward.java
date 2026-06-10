package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import unpsjb.labprog.backend.business.planificacion.domain.AgendaTaller;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.model.*;

@Component("FORWARD")
public class PlanificacionForward implements EstrategiaPlanificacion {

    @Override
    public ProcesoPlanificacion ejecutar(Producto producto, Taller taller, AgendaTaller agenda, LocalDateTime inicio) {
        List<Planificacion> planificaciones = new ArrayList<>();
        LocalDateTime tiempoActual = inicio;

        for (Tarea tarea : producto.getTareas()) {
            Equipo equipo = taller.findEquipmentForType(tarea.getTipo());
            
            LocalDateTime inicioDisponible = equipo.firstAvailableSlotAfter(tiempoActual);
            long duracion = tarea.calculateDurationFor(equipo);
            LocalDateTime fin = inicioDisponible.plusMinutes(duracion);
            
            Periodo periodo = new Periodo(inicioDisponible, fin, tarea.getTiempo());
            planificaciones.add(new Planificacion(tarea, equipo, periodo));
            
            tiempoActual = fin;
        }

        return new ProcesoPlanificacion(planificaciones, planificaciones.get(0).getPeriodo().getInicio(), tiempoActual);
    }
}