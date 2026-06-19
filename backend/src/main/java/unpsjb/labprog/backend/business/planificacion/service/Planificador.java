package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Component
public class Planificador {

    private final Map<String, EstrategiaPlanificacion> estrategias;

    public Planificador(Map<String, EstrategiaPlanificacion> estrategias) {
        this.estrategias = estrategias;
    }

    public ProcesoPlanificacion planificarHaciaAdelanteEspecífico(Producto producto, Taller taller, Agenda agenda,
            LocalDateTime inicio) {
        EstrategiaPlanificacion estrategia = estrategias.get("FORWARD");
        return estrategia.ejecutar(producto, taller, agenda, inicio);
    }

    public List<ProcesoPlanificacion> planificarPedido(
            Pedido pedido, LocalDateTime inicioLimite,
            List<Taller> talleres, Map<Long, Agenda> agendas) {

        LocalDateTime deadline = pedido.getFechaEntrega().atStartOfDay();
        int mejorCantidadPlanificable = 0;

        List<Taller> talleresOrdenados = ordenarPorDisponibilidad(talleres, agendas, deadline);

        for (Taller taller : talleresOrdenados) {
            Agenda agendaSimulacion = agendas.get(taller.getId()).copiar();
            try {
                List<ProcesoPlanificacion> procesos = planificarUnidades(pedido, taller, agendaSimulacion, deadline,
                        inicioLimite);
                agendas.put(taller.getId(), agendaSimulacion);
                pedido.markAsPlanned();
                return procesos;
            } catch (SchedulingException e) {
                mejorCantidadPlanificable = Math.max(mejorCantidadPlanificable, e.getSchedulableQuantity());
            }
        }

        pedido.markAsUnschedulable(
                "El pedido no pudo planificarse en el plazo requerido",
                mejorCantidadPlanificable > 0 ? mejorCantidadPlanificable : null);

        return List.of();
    }

    private List<ProcesoPlanificacion> planificarUnidades(Pedido pedido, Taller taller, Agenda agenda,
            LocalDateTime deadline, LocalDateTime inicioLimite) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        EstrategiaPlanificacion estrategia = estrategias.get("BACKWARD");

        for (int i = 0; i < pedido.getCantidad(); i++) {
            try {
                ProcesoPlanificacion proceso = estrategia.ejecutar(pedido.getProducto(), taller, agenda, deadline);

                if (proceso.getInicio().isBefore(inicioLimite)) {
                    throw new SchedulingException("Excede el tiempo límite de inicio", resultado.size());
                }

                proceso.setPedido(pedido);
                resultado.add(proceso);
            } catch (SchedulingException e) {
                throw new SchedulingException(e.getMessage(), resultado.size());
            }
        }
        return resultado;
    }

    private List<Taller> ordenarPorDisponibilidad(List<Taller> talleres,
            Map<Long, Agenda> agendas, LocalDateTime deadline) {

        List<Taller> resultado = new ArrayList<>(talleres);

        resultado.sort((a, b) -> Long.compare(
                agendas.get(b.getId()).calcularTiempoLibreHasta(deadline),
                agendas.get(a.getId()).calcularTiempoLibreHasta(deadline)));

        return resultado;
    }

}