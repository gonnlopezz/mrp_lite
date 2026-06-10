package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import unpsjb.labprog.backend.model.Equipment;
import unpsjb.labprog.backend.model.Period;
import unpsjb.labprog.backend.model.Planning;

@Getter
public class AgendaEquipo {

    private final Equipment equipo;
    private final List<Period> huecosLibres;

    // Ahora el constructor pide explícitamente los límites de la simulación para evitar el desfasaje de años
    public AgendaEquipo(Equipment equipo, List<Planning> planificaciones, 
                        LocalDateTime inicioHorizonte, LocalDateTime finHorizonte) {
        this.equipo = equipo;
        this.huecosLibres = new ArrayList<>();
        
        this.calcularHuecosIniciales(planificaciones, inicioHorizonte, finHorizonte);
    }

    public AgendaEquipo(AgendaEquipo original) {
        this.equipo = original.equipo;
        this.huecosLibres = new ArrayList<>(original.huecosLibres);
    }

    private void calcularHuecosIniciales(List<Planning> planificaciones, LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime cursor = inicio;
        
        for (Planning plan : planificaciones) {
            Period ocupado = plan.getPeriod();
            if (ocupado.getStart().isAfter(cursor)) {
                huecosLibres.add(new Period(cursor, ocupado.getStart(), 0));
            }
            if (ocupado.getEndDate().isAfter(cursor)) {
                cursor = ocupado.getEndDate();
            }
        }
        
        if (cursor.isBefore(fin)) {
            huecosLibres.add(new Period(cursor, fin, 0));
        }
    }

    public LocalDateTime encontrarFinBackward(LocalDateTime deadline, long duracionMinutos) {
        for (int i = huecosLibres.size() - 1; i >= 0; i--) {
            Period hueco = huecosLibres.get(i);

            LocalDateTime finEfectivo = hueco.getEndDate().isBefore(deadline) ? hueco.getEndDate() : deadline;
            LocalDateTime inicioEstimado = finEfectivo.minusMinutes(duracionMinutos);

            if (!inicioEstimado.isBefore(hueco.getStart())) {
                return finEfectivo;
            }
        }
        return null; 
    }

    public LocalDateTime primerInicioDisponibleDesde(LocalDateTime desde) {
        for (Period hueco : huecosLibres) {
            if (!hueco.getEndDate().isBefore(desde)) {
                return hueco.getStart().isAfter(desde) ? hueco.getStart() : desde;
            }
        }
        return desde;
    }

    public void ocupar(Period periodoReserva) {
        for (int i = 0; i < huecosLibres.size(); i++) {
            Period hueco = huecosLibres.get(i);

            if (!periodoReserva.getStart().isBefore(hueco.getStart()) && !periodoReserva.getEndDate().isAfter(hueco.getEndDate())) {
                huecosLibres.remove(i);
                
                if (hueco.getStart().isBefore(periodoReserva.getStart())) {
                    huecosLibres.add(i, new Period(hueco.getStart(), periodoReserva.getStart(), 0));
                    i++;
                }
                if (hueco.getEndDate().isAfter(periodoReserva.getEndDate())) {
                    huecosLibres.add(i, new Period(periodoReserva.getEndDate(), hueco.getEndDate(), 0));
                }
                break;
            }
        }
    }
}