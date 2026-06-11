package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Periodo;
import unpsjb.labprog.backend.model.Planificacion;
import unpsjb.labprog.backend.model.Taller;
import unpsjb.labprog.backend.model.Tarea;

public class Agenda {
    private final Taller taller;
    private final Map<Long, List<Periodo>> huecosPorEquipo;

    public Agenda(Taller taller, List<Planificacion> planificacionesExistentes, LocalDateTime inicio,
            LocalDateTime fin) {
        this.taller = taller;
        this.huecosPorEquipo = new HashMap<>();
        inicializarAgenda(planificacionesExistentes, inicio, fin);
    }

    private void inicializarAgenda(List<Planificacion> planificaciones, LocalDateTime inicio, LocalDateTime fin) {
        Map<Long, List<Planificacion>> pPorEquipo = new HashMap<>();
        for (Planificacion p : planificaciones) {
            Long eqId = p.getEquipo().getId();
            if (!pPorEquipo.containsKey(eqId))
                pPorEquipo.put(eqId, new ArrayList<>());
            pPorEquipo.get(eqId).add(p);
        }

        for (Equipo equipo : taller.getEquipos()) {
            List<Periodo> huecos = new ArrayList<>();
            List<Planificacion> delEquipo = pPorEquipo.getOrDefault(equipo.getId(), new ArrayList<>());

            LocalDateTime cursor = inicio;
            for (Planificacion plan : delEquipo) {
                Periodo ocupado = plan.getPeriodo();

                if (ocupado.getInicio().isAfter(cursor))
                    huecos.add(new Periodo(cursor, ocupado.getInicio(), 0));

                if (ocupado.getFin().isAfter(cursor))
                    cursor = ocupado.getFin();
            }

            if (cursor.isBefore(fin))
                huecos.add(new Periodo(cursor, fin, 0));

            if (huecos.isEmpty() && !inicio.isAfter(fin))
                huecos.add(new Periodo(inicio, fin, 0));

            huecosPorEquipo.put(equipo.getId(), huecos);
        }
    }

    /**
     * Requerimiento: Recibe la tarea completa, calcula la duración real basándose
     * en la capacidad del equipo de manera transparente y reserva el espacio.
     */
    public Periodo ocuparEspacioBackward(Tarea tarea, LocalDateTime deadlineMaximo) {
        Equipo equipo = taller.findEquipmentForType(tarea.getTipo());
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());

        long duracionEfectivaMinutos = calcularDuracionConCapacidad(tarea, equipo);

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
        return null; // No hay espacio
    }

    public Periodo ocuparEspacioForward(Tarea tarea, LocalDateTime tiempoActual) {
        Equipo equipo = taller.findEquipmentForType(tarea.getTipo());
        List<Periodo> huecos = huecosPorEquipo.get(equipo.getId());
        long duracionEfectivaMinutos = calcularDuracionConCapacidad(tarea, equipo);

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

    private long calcularDuracionConCapacidad(Tarea tarea, Equipo equipo) {
        return tarea.calculateDurationFor(equipo);
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

    public Taller getTaller() {
        return taller;
    }

    public Agenda copiar() {
        Agenda copia = new Agenda(this.taller, List.of(), LocalDateTime.MIN, LocalDateTime.MIN);
        copia.huecosPorEquipo.clear();
        for (Map.Entry<Long, List<Periodo>> entry : this.huecosPorEquipo.entrySet()) {
            copia.huecosPorEquipo.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copia;
    }
}