package unpsjb.labprog.backend.business.planificacion.domain;

import java.util.List;

import lombok.Getter;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@Getter
public class ResultadoPlanificacion {

    private final List<ProcesoPlanificacion> procesos;
    private final int cantidadPlanificada;
    private final boolean exitoso;

    private ResultadoPlanificacion(List<ProcesoPlanificacion> procesos, int cantidadPlanificada, boolean exitoso) {
        this.procesos = procesos;
        this.cantidadPlanificada = cantidadPlanificada;
        this.exitoso = exitoso;
    }

    public static ResultadoPlanificacion exitoso(List<ProcesoPlanificacion> procesos) {
        return new ResultadoPlanificacion(List.copyOf(procesos), procesos.size(), true);
    }

    public static ResultadoPlanificacion parcial(int cantidadPlanificada) {
        return new ResultadoPlanificacion(List.of(), cantidadPlanificada, false);
    }

}
