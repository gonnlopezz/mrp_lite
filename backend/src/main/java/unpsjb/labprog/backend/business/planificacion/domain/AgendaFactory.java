package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Taller;

@Component
public class AgendaFactory {

    @Autowired
    private PlanificacionRepository planificacionRepository;

    /**
     * Crea una Agenda para un único taller (usado en planificación forward manual).
     * Un único query inicial.
     */
    public Agenda crearParaTaller(Taller taller, LocalDateTime inicio, LocalDateTime fin) {
        List<Planificacion> planificaciones = planificacionRepository.planificacionesPorTaller(taller.getId());
        return new Agenda(taller, planificaciones, inicio, fin);
    }

    /**
     * Crea Agendas para múltiples talleres con UN ÚNICO query.
     * Resuelve el problema N+1 de la versión anterior.
     */
    public Map<Long, Agenda> crearParaTalleres(List<Taller> talleres,
            LocalDateTime inicio,
            LocalDateTime fin) {
        if (talleres.isEmpty())
            return Map.of();

        List<Long> tallerIds = new ArrayList<>();
        for (Taller taller : talleres) {
            tallerIds.add(taller.getId());
        }

        List<Planificacion> todas = planificacionRepository.planificacionesPorTalleres(tallerIds);

        return construirAgendas(talleres, todas, inicio, fin);
    }

    // ─── Métodos privados de construcción ────────────────────────────────────

    private Map<Long, Agenda> construirAgendas(List<Taller> talleres,
            List<Planificacion> planificaciones,
            LocalDateTime inicio,
            LocalDateTime fin) {
        Map<Long, Long> indiceEquipoATaller = indexarEquiposTaller(talleres);
        Map<Long, List<Planificacion>> porTaller = agruparPorTaller(planificaciones, indiceEquipoATaller);

        Map<Long, Agenda> agendas = new HashMap<>(); // Cambiado a HashMap común
        for (Taller taller : talleres) {
            List<Planificacion> delTaller = porTaller.getOrDefault(taller.getId(), List.of());
            agendas.put(taller.getId(), new Agenda(taller, delTaller, inicio, fin));
        }

        return agendas;
    }

    private Map<Long, Long> indexarEquiposTaller(List<Taller> talleres) {
        Map<Long, Long> indice = new HashMap<>();
        for (Taller taller : talleres) {
            for (Equipo equipo : taller.getEquipos()) {
                indice.put(equipo.getId(), taller.getId());
            }
        }
        return indice;
    }

    private Map<Long, List<Planificacion>> agruparPorTaller(List<Planificacion> planificaciones,
            Map<Long, Long> indiceEquipoATaller) {
        Map<Long, List<Planificacion>> resultado = new HashMap<>();
        for (Planificacion p : planificaciones) {
            Long tallerId = indiceEquipoATaller.get(p.getEquipo().getId());
            if (tallerId != null) {
                resultado.computeIfAbsent(tallerId, k -> new ArrayList<>()).add(p);
            }
        }
        return resultado;
    }
}