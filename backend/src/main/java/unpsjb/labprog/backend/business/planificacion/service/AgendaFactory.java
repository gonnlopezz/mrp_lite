package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Taller;

@Component
public class AgendaFactory {

    @Autowired
    private PlanificacionRepository planificacionRepository;

    public Agenda crearParaTaller(Taller taller, LocalDateTime inicio, LocalDateTime fin) {
        List<Planificacion> planificaciones = planificacionRepository.planificacionesPorTaller(taller.getId(), inicio);
        return new Agenda(taller.getEquipos(), planificaciones, inicio, fin);
    }

    public Map<Long, Agenda> crearParaTalleres(List<Taller> talleres,
            LocalDateTime inicio,
            LocalDateTime fin) {
        if (talleres.isEmpty())
            return Map.of();

        List<Planificacion> planificacionesExistentes = planificacionRepository
                .planificacionesPorTalleres(extraerTallerIds(talleres), inicio);

        return construirAgendas(talleres, planificacionesExistentes, inicio, fin);
    }

    private List<Long> extraerTallerIds(List<Taller> talleres) {
        List<Long> tallerIds = new ArrayList<>();
        for (Taller taller : talleres) {
            tallerIds.add(taller.getId());
        }
        return tallerIds;
    }

    private Map<Long, Agenda> construirAgendas(List<Taller> talleres,
            List<Planificacion> planificaciones,
            LocalDateTime inicio,
            LocalDateTime fin) {
        Map<Long, Long> indiceEquipoATaller = indexarEquiposTaller(talleres);
        Map<Long, List<Planificacion>> porTaller = agruparPorTaller(planificaciones, indiceEquipoATaller);

        Map<Long, Agenda> agendas = new HashMap<>();
        for (Taller taller : talleres) {
            List<Planificacion> delTaller = porTaller.getOrDefault(taller.getId(), List.of());
            agendas.put(taller.getId(), new Agenda(taller.getEquipos(), delTaller, inicio, fin));
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
        Map<Long, List<Planificacion>> agrupadasPorTaller = new HashMap<>();
        for (Planificacion planificacion : planificaciones) {
            Long tallerId = indiceEquipoATaller.get(planificacion.getEquipo().getId());
            if (tallerId != null) {
                agrupadasPorTaller.computeIfAbsent(tallerId, k -> new ArrayList<>()).add(planificacion);
            }
        }
        return agrupadasPorTaller;
    }
}