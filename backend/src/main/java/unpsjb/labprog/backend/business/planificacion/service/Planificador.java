package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.business.planificacion.domain.OrdenadorTaller;
import unpsjb.labprog.backend.business.planificacion.domain.ResultadoPlanificacion;
import unpsjb.labprog.backend.business.planificacion.domain.TipoEstrategia;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.*;

@Component
public class Planificador {

    private final Map<String, EstrategiaPlanificacion> estrategias;
    private final OrdenadorTaller ordenadorTaller;

    public Planificador(Map<String, EstrategiaPlanificacion> estrategias, OrdenadorTaller ordenadorTaller) {
        this.estrategias = estrategias;
        this.ordenadorTaller = ordenadorTaller;
    }

    public ProcesoPlanificacion planificarHaciaAdelante(Producto producto, Taller taller, Agenda agenda,
            LocalDateTime inicio) {
        EstrategiaPlanificacion estrategia = estrategias.get(TipoEstrategia.FORWARD);
        return estrategia.planificar(producto, taller, agenda, inicio)
                .orElseThrow(() -> new BusinessException(
                        "No se pudo planificar el producto: no hay disponibilidad en el taller"));
    }

    public List<ProcesoPlanificacion> planificarPedido(
            Pedido pedido, LocalDateTime inicioLimite,
            List<Taller> talleres, Map<Long, Agenda> agendas) {
        int mejorCantidadPlanificable = 0;

        List<Taller> talleresOrdenados = ordenadorTaller.ordenarPorDisponibilidad(talleres, agendas,
                pedido.getDeadline());

        for (Taller taller : talleresOrdenados) {
            Agenda agendaSimulacion = agendas.get(taller.getId()).copiar();
            ResultadoPlanificacion resultado = planificarUnidades(pedido, taller, agendaSimulacion, inicioLimite);

            if (resultado.isExitoso()) {
                agendas.put(taller.getId(), agendaSimulacion);
                pedido.marcarComoPlanificado();
                return resultado.getProcesos();
            } else {
                mejorCantidadPlanificable = Math.max(mejorCantidadPlanificable, resultado.getCantidadPlanificada());
            }
        }

        pedido.marcarComoNoPlanificable(
                "El pedido no pudo planificarse en el plazo requerido",
                mejorCantidadPlanificable > 0 ? mejorCantidadPlanificable : null);

        return List.of();
    }

    private ResultadoPlanificacion planificarUnidades(Pedido pedido, Taller taller, Agenda agenda,
            LocalDateTime inicioLimite) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        EstrategiaPlanificacion estrategia = estrategias.get(TipoEstrategia.BACKWARD);

        for (int i = 0; i < pedido.getCantidad(); i++) {
            Optional<ProcesoPlanificacion> proceso = estrategia.planificar(pedido.getProducto(), taller, agenda,
                    pedido.getDeadline());

            if (proceso.isEmpty() || proceso.get().getInicio().isBefore(inicioLimite))
                return ResultadoPlanificacion.parcial(resultado.size());

            proceso.get().setPedido(pedido);
            resultado.add(proceso.get());
        }
        return ResultadoPlanificacion.exitoso(resultado);
    }

}