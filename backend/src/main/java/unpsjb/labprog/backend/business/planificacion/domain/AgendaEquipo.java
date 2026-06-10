package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Periodo;
import unpsjb.labprog.backend.model.Planificacion;

@Getter
public class AgendaEquipo {

    private final Equipo equipo;
    private final List<Periodo> huecosLibres;

    public AgendaEquipo(Equipo equipo, List<Planificacion> planificaciones, 
                        LocalDateTime inicioHorizonte, LocalDateTime finHorizonte) {
        this.equipo = equipo;
        this.huecosLibres = new ArrayList<>();
        
        this.calcularHuecosIniciales(planificaciones, inicioHorizonte, finHorizonte);
    }

    public AgendaEquipo(AgendaEquipo original) {
        this.equipo = original.equipo;
        this.huecosLibres = new ArrayList<>(original.huecosLibres);
    }

    private void calcularHuecosIniciales(List<Planificacion> planificaciones, LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime cursor = inicio;
        
        for (Planificacion plan : planificaciones) {
            Periodo ocupado = plan.getPeriodo();
            if (ocupado.getInicio().isAfter(cursor)) {
                huecosLibres.add(new Periodo(cursor, ocupado.getInicio(), 0));
            }
            if (ocupado.getFin().isAfter(cursor)) {
                cursor = ocupado.getFin();
            }
        }
        
        if (cursor.isBefore(fin)) {
            huecosLibres.add(new Periodo(cursor, fin, 0));
        }
    }

    public LocalDateTime encontrarFinBackward(LocalDateTime deadline, long duracionMinutos) {
        for (int i = huecosLibres.size() - 1; i >= 0; i--) {
            Periodo hueco = huecosLibres.get(i);

            LocalDateTime finEfectivo = hueco.getFin().isBefore(deadline) ? hueco.getFin() : deadline;
            LocalDateTime inicioEstimado = finEfectivo.minusMinutes(duracionMinutos);

            if (!inicioEstimado.isBefore(hueco.getInicio())) {
                return finEfectivo;
            }
        }
        return null; 
    }

    public LocalDateTime primerInicioDisponibleDesde(LocalDateTime tiempoActual) {
        for (Periodo hueco : huecosLibres) {
            if (!hueco.getFin().isBefore(tiempoActual)) {
                return hueco.getInicio().isAfter(tiempoActual) ? hueco.getInicio() : tiempoActual;
            }
        }
        return tiempoActual;
    }

    public void ocupar(Periodo periodoReserva) {
        int i = 0;
        boolean buscando = true;

        while (i < huecosLibres.size() && buscando) {
            Periodo hueco = huecosLibres.get(i);

            if (!periodoReserva.getInicio().isBefore(hueco.getInicio()) && !periodoReserva.getFin().isAfter(hueco.getFin())) {
                
                huecosLibres.remove(i);
                
                if (hueco.getInicio().isBefore(periodoReserva.getInicio())) {
                    huecosLibres.add(i, new Periodo(hueco.getInicio(), periodoReserva.getInicio(), 0));
                    i++;
                }
                
                if (hueco.getFin().isAfter(periodoReserva.getFin())) {
                    huecosLibres.add(i, new Periodo(periodoReserva.getFin(), hueco.getFin(), 0));
                }
                
                buscando = false;
            } else {
                i++;
            }
        }
    }

    
}