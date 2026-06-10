package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Taller;

public class AgendaTaller {
    private final Taller taller;
    private final Map<Long, AgendaEquipo> agendasPorEquipoId;

    private AgendaTaller(Taller taller, Map<Long, AgendaEquipo> agendas) {
        this.taller = taller;
        this.agendasPorEquipoId = agendas;
    }

    public static AgendaTaller construirDesde(Taller taller, List<Planificacion> planificaciones,
            LocalDateTime inicio, LocalDateTime fin) {

        Map<Long, List<Planificacion>> porEquipo = new HashMap<>();
        for (Planificacion planificacion : planificaciones) {
            Long equipoId = planificacion.getEquipo().getId();
            porEquipo.computeIfAbsent(equipoId, k -> new ArrayList<>()).add(planificacion);
        }

        Map<Long, AgendaEquipo> agendas = new HashMap<>();
        for (Equipo equipo : taller.getEquipos()) {
            List<Planificacion> planificacionesEquipo = porEquipo.getOrDefault(equipo.getId(), List.of());
            agendas.put(equipo.getId(), new AgendaEquipo(equipo, planificacionesEquipo, inicio, fin));
        }

        return new AgendaTaller(taller, agendas);
    }

    public static Map<Long, AgendaTaller> construirTodasDesde(
            List<Taller> talleres, List<Planificacion> planificaciones,
            LocalDateTime inicio, LocalDateTime fin) {
        
        Map<Long, List<Planificacion>> porEquipo = new HashMap<>();
        for (Planificacion p : planificaciones) {
            porEquipo.computeIfAbsent(p.getEquipo().getId(), k -> new ArrayList<>()).add(p);
        }

        Map<Long, AgendaTaller> agendas = new HashMap<>();
        for (Taller taller : talleres) {
            List<Planificacion> planificacionesTaller = new ArrayList<>();
            for (Equipo equipo : taller.getEquipos()) {
                List<Planificacion> delEquipo = porEquipo.get(equipo.getId());
                if (delEquipo != null) {
                    planificacionesTaller.addAll(delEquipo);
                }
            }
            agendas.put(taller.getId(), AgendaTaller.construirDesde(taller, planificacionesTaller, inicio, fin));
        }
        return agendas;
    }

    public AgendaEquipo agendaDe(Equipo equipo) {
        return agendasPorEquipoId.get(equipo.getId());
    }

    public Taller getTaller() {
        return taller;
    }

    public AgendaTaller copiar() {
        Map<Long, AgendaEquipo> copiaAgendas = new HashMap<>();
        for (Map.Entry<Long, AgendaEquipo> entrada : agendasPorEquipoId.entrySet()) {
            copiaAgendas.put(entrada.getKey(), new AgendaEquipo(entrada.getValue()));
        }
        return new AgendaTaller(this.taller, copiaAgendas);
    }
}