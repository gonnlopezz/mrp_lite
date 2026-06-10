package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import unpsjb.labprog.backend.model.Equipment;
import unpsjb.labprog.backend.model.Planning;
import unpsjb.labprog.backend.model.Workshop;

public class AgendaTaller {
    private final Workshop taller;
    private final Map<Long, AgendaEquipo> agendasPorEquipoId;

    private AgendaTaller(Workshop taller, Map<Long, AgendaEquipo> agendas) {
        this.taller = taller;
        this.agendasPorEquipoId = agendas;
    }

    public static AgendaTaller construirDesde(Workshop taller, List<Planning> planificaciones, 
                                              LocalDateTime inicio, LocalDateTime fin) {
        Map<Long, List<Planning>> porEquipo = new HashMap<>();
        for (Planning planificacion : planificaciones) {
            Long equipoId = planificacion.getEquipment().getId();
            if (!porEquipo.containsKey(equipoId)) {
                porEquipo.put(equipoId, new ArrayList<>());
            }
            porEquipo.get(equipoId).add(planificacion);
        }

        Map<Long, AgendaEquipo> agendas = new HashMap<>();
        for (Equipment equipo : taller.getEquipments()) {
            List<Planning> planificacionesEquipo = porEquipo.get(equipo.getId());
            if (planificacionesEquipo == null) {
                planificacionesEquipo = new ArrayList<>();
            }
            // Pasamos las variables temporales del horizonte
            agendas.put(equipo.getId(), new AgendaEquipo(equipo, planificacionesEquipo, inicio, fin));
        }

        return new AgendaTaller(taller, agendas);
    }

    public AgendaEquipo agendaDe(Equipment equipo) {
        return agendasPorEquipoId.get(equipo.getId());
    }

    public Workshop getTaller() { 
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