package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import org.springframework.stereotype.Component;

import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Component("BACKWARD")
public class PlanificacionBackward implements EstrategiaPlanificacion {

    @Override
    public ProcesoPlanificacion ejecutar(Producto producto, Taller taller, Agenda agenda, LocalDateTime deadline) {
        LinkedList<Planificacion> planificaciones = new LinkedList<>();
        LocalDateTime finActual = deadline;

        for (Tarea tarea : producto.getReverseTasks()) {
            Equipo equipo = taller.findEquipmentForType(tarea.getTipo());

            Periodo periodo = agenda.ocuparEspacioBackward(tarea, finActual);

            if (periodo == null) {
                throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido",
                        planificaciones.size());
            }

            planificaciones.addFirst(new Planificacion(tarea, equipo, periodo));
            finActual = periodo.getInicio();
        }

        return new ProcesoPlanificacion(planificaciones, planificaciones.getFirst().getPeriodo().getInicio(), deadline);
    }
}