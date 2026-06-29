package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.business.planificacion.domain.TipoEstrategia;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Component(TipoEstrategia.FORWARD)
public class PlanificacionForward implements EstrategiaPlanificacion {

    @Override
    public ProcesoPlanificacion planificar(Producto producto, Taller taller, Agenda agenda, LocalDateTime inicio) {
        List<Planificacion> planificaciones = new ArrayList<>();
        LocalDateTime tiempoActual = inicio;

        for (Tarea tarea : producto.getTareas()) {
            Equipo equipo = taller.encontrarEquipamientoPara(tarea.getTipo());

            Periodo periodo = agenda.ocuparEspacioForward(tarea, equipo, tiempoActual);

            if (periodo == null)
                throw new SchedulingException("No se encontró hueco hacia adelante para la tarea: " + tarea.getNombre(),
                        planificaciones.size());

            planificaciones.add(new Planificacion(tarea, equipo, periodo));

            tiempoActual = periodo.getFin();
        }

        return new ProcesoPlanificacion(planificaciones, planificaciones.get(0).getPeriodo().getInicio(), tiempoActual);
    }
}