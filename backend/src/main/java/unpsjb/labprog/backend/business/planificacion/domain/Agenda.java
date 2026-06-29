package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Periodo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Tarea;

public class Agenda {

    private final Map<Long, List<Periodo>> huecosPorEquipo;

    public Agenda(Collection<Equipo> equipos, List<Planificacion> planificacionesExistentes, LocalDateTime inicio,
            LocalDateTime fin) {
        this.huecosPorEquipo = new HashMap<>();

        inicializarAgenda(equipos, new ArrayList<>(planificacionesExistentes), inicio, fin);
    }

    private Agenda(Map<Long, List<Periodo>> huecos) {
        this.huecosPorEquipo = new HashMap<>();
        huecos.forEach((k, v) -> this.huecosPorEquipo.put(k, new ArrayList<>(v)));
    }

    private Map<Long, List<Planificacion>> agruparPorEquipo(List<Planificacion> planificaciones) {
        Map<Long, List<Planificacion>> agrupadasPorEquipo = new HashMap<>();
        for (Planificacion planificacion : planificaciones) {
            agrupadasPorEquipo.computeIfAbsent(planificacion.getEquipo().getId(), k -> new ArrayList<>())
                    .add(planificacion);
        }
        return agrupadasPorEquipo;
    }

    private void inicializarAgenda(Collection<Equipo> equipos, List<Planificacion> planificaciones,
            LocalDateTime inicio, LocalDateTime fin) {
        Map<Long, List<Planificacion>> planificacionesPorEquipo = agruparPorEquipo(planificaciones);

        for (Equipo equipo : equipos) {
            List<Planificacion> planificacionesDelEquipo = planificacionesPorEquipo.getOrDefault(equipo.getId(),
                    List.of());
            List<Periodo> huecos = calcularHuecosParaEquipo(planificacionesDelEquipo, inicio, fin);
            huecosPorEquipo.put(equipo.getId(), huecos);
        }
    }

    private List<Periodo> calcularHuecosParaEquipo(List<Planificacion> planificaciones,
            LocalDateTime inicio, LocalDateTime fin) {
        List<Periodo> huecos = new ArrayList<>();
        LocalDateTime cursor = inicio;

        for (Planificacion planificacion : planificaciones) {
            Periodo periodoReservado = planificacion.getPeriodo();
            if (periodoReservado.getInicio().isAfter(cursor)) {
                huecos.add(new Periodo(cursor, periodoReservado.getInicio(), 0));
            }

            if (periodoReservado.getFin().isAfter(cursor)) {
                cursor = periodoReservado.getFin();
            }
        }

        if (cursor.isBefore(fin)) {
            huecos.add(new Periodo(cursor, fin, 0));
        }

        return huecos;
    }

    public Optional<Periodo> ocuparEspacioForward(Tarea tarea, Equipo equipo, LocalDateTime tiempoActual) {
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());
        if (huecos == null)
            return Optional.empty();

        for (int i = 0; i < huecos.size(); i++) {
            Periodo hueco = huecos.get(i);
            if (!hueco.getFin().isBefore(tiempoActual)) {
                LocalDateTime inicioEfectivo = hueco.getInicio().isAfter(tiempoActual) ? hueco.getInicio()
                        : tiempoActual;
                LocalDateTime finEstimado = inicioEfectivo.plusMinutes(tarea.calcularDuracionPara(equipo));

                if (!finEstimado.isAfter(hueco.getFin())) {
                    Periodo nuevoPeriodoOcupado = new Periodo(inicioEfectivo, finEstimado, tarea.getTiempo());
                    actualizarHuecos(huecos, i, nuevoPeriodoOcupado);
                    return Optional.of(nuevoPeriodoOcupado);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Periodo> ocuparEspacioBackward(Tarea tarea, Equipo equipo, LocalDateTime deadline) {
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());
        if (huecos == null)
            return Optional.empty();

        for (int i = huecos.size() - 1; i >= 0; i--) {
            Periodo hueco = huecos.get(i);
            LocalDateTime fin = hueco.getFin().isBefore(deadline) ? hueco.getFin() : deadline;
            LocalDateTime inicio = fin.minusMinutes(tarea.calcularDuracionPara(equipo));

            if (!inicio.isBefore(hueco.getInicio())) {
                Periodo nuevoPeriodoOcupado = new Periodo(inicio, fin, tarea.getTiempo());
                actualizarHuecos(huecos, i, nuevoPeriodoOcupado);
                return Optional.of(nuevoPeriodoOcupado);
            }
        }
        return Optional.empty();
    }

    private void actualizarHuecos(List<Periodo> huecos, int indiceHuecoAfectado, Periodo ocupado) {
        Periodo huecoOriginal = huecos.remove(indiceHuecoAfectado);

        if (huecoOriginal.getInicio().isBefore(ocupado.getInicio())) {
            huecos.add(indiceHuecoAfectado, new Periodo(huecoOriginal.getInicio(), ocupado.getInicio(), 0));
            indiceHuecoAfectado++;
        }

        if (huecoOriginal.getFin().isAfter(ocupado.getFin()))
            huecos.add(indiceHuecoAfectado, new Periodo(ocupado.getFin(), huecoOriginal.getFin(), 0));
    }

    public long calcularMinutosLibresHasta(LocalDateTime deadline) {
        long resultado = 0;

        for (List<Periodo> huecos : huecosPorEquipo.values()) {
            for (Periodo hueco : huecos) {
                resultado += minutosLibresEn(hueco, deadline);
            }
        }

        return resultado;
    }

    private long minutosLibresEn(Periodo hueco, LocalDateTime deadline) {
        if (hueco.getInicio().isAfter(deadline))
            return 0;

        LocalDateTime finEfectivo = hueco.getFin().isAfter(deadline) ? deadline : hueco.getFin();
        return Math.max(0, ChronoUnit.MINUTES.between(hueco.getInicio(), finEfectivo));
    }

    public Agenda copiar() {
        return new Agenda(this.huecosPorEquipo);
    }
}