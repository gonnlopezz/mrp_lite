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
    private PlanificacionRepository planningRepository; // Repositorio directo

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
        List<ProcesoPlanificacion> resultado = planificarPedidoSeguro(pedido, inicioLimite, new HashMap<>());

        orderService.save(pedido);
        return resultado;
    }

    public List<ProcesoPlanificacion> planBulkOrders(
            List<Pedido> pedidos, LocalDateTime inicioEjecucion) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        Map<Long, AgendaTaller> agendasTalleres = new HashMap<>(); // Reutiliza agendas entre pedidos del lote

        for (Pedido pedido : pedidos) {
            resultado.addAll(planificarPedidoSeguro(pedido, inicioEjecucion, agendasTalleres));
        }

        orderService.saveAll(pedidos);
        return resultado;
    }

    private List<ProcesoPlanificacion> planificarPedidoSeguro(
            Pedido pedido, LocalDateTime inicioLimite,
            Map<Long, AgendaTaller> agendasTalleres) {
        try {
            List<Taller> talleres = workshopService.findPossibleWorkshops(
                    pedido.getProducto().requiredEquipmentTypes());

            List<ProcesoPlanificacion> procesos = planificarEnPrimerTallerDisponible(
                    pedido, talleres, pedido.getFechaEntrega().atStartOfDay(), inicioLimite, agendasTalleres);

            pedido.markAsPlanned();
            return procesos;
        } catch (SchedulingException e) {
            pedido.markAsUnschedulable(e.getMessage(), positiveOrNull(e.getSchedulableQuantity()));
            return List.of();
        } catch (BusinessException e) {
            pedido.markAsUnschedulable(e.getMessage(), null);
            return List.of();
        }
    }

    private List<ProcesoPlanificacion> planificarEnPrimerTallerDisponible(
            Pedido pedido, List<Taller> talleres,
            LocalDateTime deadline, LocalDateTime inicioLimite,
            Map<Long, AgendaTaller> agendasTalleres) {
        int mejorCantidadPlanificable = 0;

        for (Taller taller : talleres) {
            AgendaTaller agendaSimulacion = obtenerAgendaParaSimulacion(taller, agendasTalleres, inicioLimite, deadline);
            try {
                List<ProcesoPlanificacion> procesos = planificarUnidades(
                        pedido, taller, agendaSimulacion, deadline, inicioLimite);

                agendasTalleres.put(taller.getId(), agendaSimulacion);
                return procesos;
            } catch (SchedulingException e) {
                mejorCantidadPlanificable = Math.max(mejorCantidadPlanificable, e.getSchedulableQuantity());
            } catch (BusinessException ignored) {
                // Taller sin el equipamiento necesario configurado; ignorar y evaluar el
                // siguiente
            }
        }

        throw new SchedulingException(
                "El pedido no pudo planificarse en el plazo requerido", mejorCantidadPlanificable);
    }

    private AgendaTaller obtenerAgendaParaSimulacion(
            Taller taller, Map<Long, AgendaTaller> cache, LocalDateTime inicio, LocalDateTime fin) {
        if (cache.containsKey(taller.getId())) {
            return cache.get(taller.getId()).copiar();
        }

        List<Planificacion> planificaciones = planningRepository.findPlanificacionesPorTaller(taller.getId());
        return AgendaTaller.construirDesde(taller, planificaciones, inicio, fin);
    }

    private List<ProcesoPlanificacion> planificarUnidades(
            Pedido pedido, Taller taller, AgendaTaller agenda,
            LocalDateTime deadline, LocalDateTime inicioLimite) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();

        EstrategiaPlanificacion estrategia = estrategias.get("BACKWARD");

        for (int i = 0; i < pedido.getCantidad(); i++) {
            ProcesoPlanificacion proceso = estrategia.ejecutar(
                    pedido.getProducto(), taller, agenda, deadline);

            if (proceso.getInicio().isBefore(inicioLimite)) {
                throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido", resultado.size());
            }

            proceso.setPedido(pedido);
            resultado.add(proceso);
        }
        return resultado;
    }

    private Integer positiveOrNull(int value) {
        return value > 0 ? value : null;
    }
}