package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Periodo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Producto;
import unpsjb.labprog.backend.model.Taller;
import unpsjb.labprog.backend.model.Tarea;

public abstract class EstrategiaPlanificacionBase implements EstrategiaPlanificacion {
    public ProcesoPlanificacion planificar(Producto producto, Taller taller, Agenda agenda,
            LocalDateTime fechaReferencia) {
        LinkedList<Planificacion> planificaciones = new LinkedList<>();
        LocalDateTime cursor = fechaReferencia;

        for (Tarea tarea : obtenerTareas(producto)) {
            Equipo equipo = taller.encontrarEquipamientoPara(tarea.getTipo());
            Periodo periodoReservado = ocuparHueco(agenda, tarea, equipo, cursor);

            if (periodoReservado == null)
                throw new SchedulingException("No se encontró hueco para la tarea: " + tarea.getNombre(),
                        planificaciones.size());

            agregarPlanificacion(planificaciones, new Planificacion(tarea, equipo, periodoReservado));
            cursor = avanzarCursor(periodoReservado);

        }
        return new ProcesoPlanificacion(planificaciones, obtenerInicio(planificaciones),
                calcularFin(fechaReferencia, cursor));
    }

    private LocalDateTime obtenerInicio(LinkedList<Planificacion> planificaciones) {
        return planificaciones.getFirst().getPeriodo().getInicio();
    }

    protected abstract List<Tarea> obtenerTareas(Producto producto);

    protected abstract Periodo ocuparHueco(Agenda agenda, Tarea tarea, Equipo equipo, LocalDateTime cursor);

    protected abstract void agregarPlanificacion(LinkedList<Planificacion> planificaciones,
            Planificacion planificacion);

    protected abstract LocalDateTime avanzarCursor(Periodo periodoReservado);

    protected abstract LocalDateTime calcularFin(LocalDateTime fechaReferencia, LocalDateTime cursorFinal);

}
