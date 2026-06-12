package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Periodo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Tarea;

public class Agenda {

    private final Map<Long, List<Periodo>> huecosPorEquipo;

    public Agenda(Collection<Equipo> equipos, List<Planificacion> planificacionesExistentes, LocalDateTime inicio,
            LocalDateTime fin) {
        this.huecosPorEquipo = new HashMap<>();

        List<Planificacion> planificaciones = new ArrayList<>(planificacionesExistentes);

        inicializarAgenda(equipos, planificaciones, inicio, fin);
    }

    private Agenda(Map<Long, List<Periodo>> huecos) {
        this.huecosPorEquipo = new HashMap<>();
        huecos.forEach((k, v) -> this.huecosPorEquipo.put(k, new ArrayList<>(v)));
    }

    private void inicializarAgenda(Collection<Equipo> equipos, List<Planificacion> planificaciones,
            LocalDateTime inicio, LocalDateTime fin) {
        Map<Long, List<Planificacion>> pPorEquipo = new HashMap<>();
        for (Planificacion p : planificaciones) {
            pPorEquipo.computeIfAbsent(p.getEquipo().getId(), k -> new ArrayList<>()).add(p);
        }

        for (Equipo equipo : equipos) {
            List<Periodo> huecos = new ArrayList<>();
            List<Planificacion> delEquipo = pPorEquipo.getOrDefault(equipo.getId(), List.of());

            LocalDateTime cursor = inicio;
            for (Planificacion plan : delEquipo) {
                Periodo ocupado = plan.getPeriodo();
                if (ocupado.getInicio().isAfter(cursor)) {
                    huecos.add(new Periodo(cursor, ocupado.getInicio(), 0));
                }
                if (ocupado.getFin().isAfter(cursor)) {
                    cursor = ocupado.getFin();
                }
            }

            if (cursor.isBefore(fin)) {
                huecos.add(new Periodo(cursor, fin, 0));
            }

            if (huecos.isEmpty() && !inicio.isAfter(fin)) {
                huecos.add(new Periodo(inicio, fin, 0));
            }

            huecosPorEquipo.put(equipo.getId(), huecos);
        }
    }

    /**
     * Evalúa y reserva el espacio en la línea de tiempo hacia adelante.
     */
    public Periodo ocuparEspacioForward(Tarea tarea, Equipo equipo, LocalDateTime tiempoActual) {
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());
        if (huecos == null)
            return null;

        long duracionEfectivaMinutos = tarea.calculateDurationFor(equipo);

        for (int i = 0; i < huecos.size(); i++) {
            Periodo hueco = huecos.get(i);
            if (!hueco.getFin().isBefore(tiempoActual)) {
                LocalDateTime inicioEfectivo = hueco.getInicio().isAfter(tiempoActual) ? hueco.getInicio()
                        : tiempoActual;
                LocalDateTime finEstimado = inicioEfectivo.plusMinutes(duracionEfectivaMinutos);

                if (!finEstimado.isAfter(hueco.getFin())) {
                    Periodo nuevoPeriodoOcupado = new Periodo(inicioEfectivo, finEstimado, tarea.getTiempo());
                    actualizarHuecos(huecos, i, nuevoPeriodoOcupado);
                    return nuevoPeriodoOcupado;
                }
            }
        }
        return null;
    }

    public Periodo ocuparEspacioBackward(Tarea tarea, Equipo equipo, LocalDateTime deadlineMaximo) {
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());
        if (huecos == null)
            return null;

        long duracionEfectivaMinutos = tarea.calculateDurationFor(equipo);

        for (int i = huecos.size() - 1; i >= 0; i--) {
            Periodo hueco = huecos.get(i);
            LocalDateTime finEfectivo = hueco.getFin().isBefore(deadlineMaximo) ? hueco.getFin() : deadlineMaximo;
            LocalDateTime inicioEstimado = finEfectivo.minusMinutes(duracionEfectivaMinutos);

            if (!inicioEstimado.isBefore(hueco.getInicio())) {
                Periodo nuevoPeriodoOcupado = new Periodo(inicioEstimado, finEfectivo, tarea.getTiempo());
                actualizarHuecos(huecos, i, nuevoPeriodoOcupado);
                return nuevoPeriodoOcupado;
            }
        }
        return null;
    }

    public long obtenerTotalMinutosDisponibles() {
        long totalMinutos = 0;

        for (List<Periodo> huecos : huecosPorEquipo.values()) {
            for (Periodo hueco : huecos) {
                long minutos = java.time.Duration.between(hueco.getInicio(), hueco.getFin()).toMinutes();
                totalMinutos += minutos;
            }
        }
        return totalMinutos;
    }

    private void actualizarHuecos(List<Periodo> huecos, int indiceHuecoAfectado, Periodo ocupado) {
        Periodo huecoOriginal = huecos.remove(indiceHuecoAfectado);

        if (huecoOriginal.getInicio().isBefore(ocupado.getInicio())) {
            huecos.add(indiceHuecoAfectado, new Periodo(huecoOriginal.getInicio(), ocupado.getInicio(), 0));
            indiceHuecoAfectado++;
        }

        if (huecoOriginal.getFin().isAfter(ocupado.getFin())) {
            huecos.add(indiceHuecoAfectado, new Periodo(ocupado.getFin(), huecoOriginal.getFin(), 0));
        }
    }

    /**
     * Clonación profunda para Rollback seguro durante las simulaciones del
     * Planificador.
     */
    public Agenda copiar() {
        return new Agenda(this.huecosPorEquipo);
    }
}