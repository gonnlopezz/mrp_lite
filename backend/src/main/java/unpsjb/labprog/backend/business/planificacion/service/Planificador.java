package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.business.planificacion.domain.AgendaTaller;
import unpsjb.labprog.backend.business.planificacion.domain.EstrategiaPlanificacion;
import unpsjb.labprog.backend.business.producto.ProductoService;
import unpsjb.labprog.backend.business.taller.TallerService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Service
public class Planificador {

    @Autowired
    private ProductoService productService;
    @Autowired
    private TallerService workshopService;
    @Autowired
    private PedidoService orderService;
    @Autowired
    private PlanificacionRepository planningRepository;

    private final Map<String, EstrategiaPlanificacion> estrategias;

    @Autowired
    public Planificador(Map<String, EstrategiaPlanificacion> estrategias) {
        this.estrategias = estrategias;
    }

    public ProcesoPlanificacion planForward(PlanningRequestDTO request) {
        LocalDateTime inicio = request.getStartDate().toLocalDate().atStartOfDay();
        Producto producto = productService.findByName(request.getProductName());
        Taller taller = workshopService.resolveWorkshop(
                request.getWorkshopCode(), producto.requiredEquipmentTypes());

        EstrategiaPlanificacion estrategia = estrategias.get("FORWARD");
        return estrategia.ejecutar(producto, taller, null, inicio);
    }

    public List<ProcesoPlanificacion> planBackward(PlanningFromOrderRequestDTO request) {
        Pedido pedido = orderService.findById(request.getOrder().getId());
        pedido.validatePlannable();

        LocalDateTime inicioLimite = request.getStartDate().toLocalDate().atStartOfDay();
        LocalDateTime deadline = pedido.getFechaEntrega().atStartOfDay();

        List<Taller> talleresPosibles;
        try {
            talleresPosibles = workshopService.findPossibleWorkshops(pedido.getProducto().requiredEquipmentTypes());
        } catch (BusinessException e) {
            pedido.markAsUnschedulable(e.getMessage(), null);
            orderService.save(pedido);
            return List.of(); 
        }

        Map<Long, AgendaTaller> agendas = new HashMap<>();
        for (Taller taller : talleresPosibles) {
            List<Planificacion> delTaller = planningRepository.findPlanificacionesPorTaller(taller.getId());
            agendas.put(taller.getId(), AgendaTaller.construirDesde(taller, delTaller, inicioLimite, deadline));
        }

        List<ProcesoPlanificacion> resultado = planificarPedido(pedido, inicioLimite, talleresPosibles, agendas);

        orderService.save(pedido);
        return resultado;
    }

    public List<ProcesoPlanificacion> planBulkOrders(List<Pedido> pedidos, LocalDateTime inicioEjecucion) {
        if (pedidos.isEmpty())
            return List.of();

        LocalDateTime deadlineMaxima = pedidos.get(pedidos.size() - 1).getFechaEntrega().atStartOfDay();

        List<Taller> todosTalleres = workshopService.findAll();
        List<Planificacion> todasLasPlanificaciones = planningRepository.findAllPlanificacionesOrdenadas();

        Map<Long, AgendaTaller> agendas = AgendaTaller.construirTodasDesde(
                todosTalleres, todasLasPlanificaciones, inicioEjecucion, deadlineMaxima);

        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            resultado.addAll(planificarPedido(pedido, inicioEjecucion, todosTalleres, agendas));
        }

        orderService.saveAll(pedidos);
        return resultado;
    }

    private List<ProcesoPlanificacion> planificarPedido(
            Pedido pedido, LocalDateTime inicioLimite,
            List<Taller> talleres, Map<Long, AgendaTaller> agendas) {

        List<Taller> talleresPosibles;
        try {
            talleresPosibles = workshopService.findPossibleWorkshops(pedido.getProducto().requiredEquipmentTypes());
        } catch (BusinessException e) {
            pedido.markAsUnschedulable(e.getMessage(), null);
            return List.of();
        }

        LocalDateTime deadline = pedido.getFechaEntrega().atStartOfDay();
        int mejorCantidadPlanificable = 0;

        for (Taller taller : talleresPosibles) {
            AgendaTaller agendaSimulacion = agendas.get(taller.getId()).copiar();
            try {
                List<ProcesoPlanificacion> procesos = planificarUnidades(pedido, taller, agendaSimulacion, deadline,
                        inicioLimite);

                agendas.put(taller.getId(), agendaSimulacion);
                pedido.markAsPlanned();
                return procesos;
            } catch (SchedulingException e) {
                mejorCantidadPlanificable = Math.max(mejorCantidadPlanificable, e.getSchedulableQuantity());
            } catch (BusinessException ignored) {
                // Taller sin el equipamiento necesario configurado
            }
        }

        pedido.markAsUnschedulable("El pedido no pudo planificarse en el plazo requerido",
                esPositivo(mejorCantidadPlanificable));
        return List.of();
    }

    private List<ProcesoPlanificacion> planificarUnidades(Pedido pedido, Taller taller, AgendaTaller agenda,
            LocalDateTime deadline, LocalDateTime inicioLimite) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        EstrategiaPlanificacion estrategia = estrategias.get("BACKWARD");

        for (int i = 0; i < pedido.getCantidad(); i++) {
            ProcesoPlanificacion proceso = estrategia.ejecutar(pedido.getProducto(), taller, agenda, deadline);

            if (proceso.getInicio().isBefore(inicioLimite)) {
                throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido", resultado.size());
            }

            proceso.setPedido(pedido);
            resultado.add(proceso);
        }
        return resultado;
    }

    private Integer esPositivo(int value) {
        return value > 0 ? value : null;
    }
}