package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.business.product.ProductService;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.*;

@Service
public class Planificador {

    @Autowired
    private ProductService productService;

    @Autowired
    private WorkshopService workshopService;

    @Autowired
    private ManufacturingOrderService orderService;


    @Autowired
    private PlanningProcessRepository planningRepository; // Repositorio directo

    private final Map<String, EstrategiaPlanificacion> estrategias;

    @Autowired
    public Planificador(Map<String, EstrategiaPlanificacion> estrategias) {
        this.estrategias = estrategias;
    }

    public PlanningProcess planForward(PlanningRequestDTO request) {
        LocalDateTime inicio = request.getStartDate().toLocalDate().atStartOfDay();
        Product producto = productService.findByName(request.getProductName());
        Workshop taller = workshopService.resolveWorkshop(
                request.getWorkshopCode(), producto.requiredEquipmentTypes());

        EstrategiaPlanificacion estrategia = estrategias.get("FORWARD");
        return estrategia.ejecutar(producto, taller, null, inicio);
    }

    public List<PlanningProcess> planBackward(PlanningFromOrderRequestDTO request) {
        ManufacturingOrder pedido = orderService.findById(request.getOrder().getId());
        pedido.validatePlannable();

        LocalDateTime inicioLimite = request.getStartDate().toLocalDate().atStartOfDay();
        List<PlanningProcess> resultado = planificarPedidoSeguro(pedido, inicioLimite, new HashMap<>());

        orderService.save(pedido);
        return resultado;
    }

    public List<PlanningProcess> planBulkOrders(
            List<ManufacturingOrder> pedidos, LocalDateTime inicioEjecucion) {
        List<PlanningProcess> resultado = new ArrayList<>();
        Map<Long, AgendaTaller> agendasTalleres = new HashMap<>(); // Reutiliza agendas entre pedidos del lote

        for (ManufacturingOrder pedido : pedidos) {
            resultado.addAll(planificarPedidoSeguro(pedido, inicioEjecucion, agendasTalleres));
        }

        orderService.saveAll(pedidos);
        return resultado;
    }

    private List<PlanningProcess> planificarPedidoSeguro(
            ManufacturingOrder pedido, LocalDateTime inicioLimite,
            Map<Long, AgendaTaller> agendasTalleres) {
        try {
            List<Workshop> talleres = workshopService.findPossibleWorkshops(
                    pedido.getProduct().requiredEquipmentTypes());

            List<PlanningProcess> procesos = planificarEnPrimerTallerDisponible(
                    pedido, talleres, pedido.getDeliveryDate().atStartOfDay(), inicioLimite, agendasTalleres);

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

    private List<PlanningProcess> planificarEnPrimerTallerDisponible(
            ManufacturingOrder pedido, List<Workshop> talleres,
            LocalDateTime deadline, LocalDateTime inicioLimite,
            Map<Long, AgendaTaller> agendasTalleres) {
        int mejorCantidadPlanificable = 0;

        for (Workshop taller : talleres) {
            // 2. Obtenemos la agenda pasando únicamente los 2 parámetros que tu clase
            // requiere
            AgendaTaller agendaSimulacion = obtenerAgendaParaSimulacion(taller, agendasTalleres, inicioLimite, deadline);
            try {
                List<PlanningProcess> procesos = planificarUnidades(
                        pedido, taller, agendaSimulacion, deadline, inicioLimite);

                // Confirmamos la simulación exitosa en el caché persistente del lote
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
            Workshop taller, Map<Long, AgendaTaller> cache, LocalDateTime inicio, LocalDateTime fin) {
        if (cache.containsKey(taller.getId())) {
            return cache.get(taller.getId()).copiar();
        }

        List<Planning> planificaciones = planningRepository.findPlanificacionesPorTaller(taller.getId());
        return AgendaTaller.construirDesde(taller, planificaciones, inicio, fin);
    }

    private List<PlanningProcess> planificarUnidades(
            ManufacturingOrder pedido, Workshop taller, AgendaTaller agenda,
            LocalDateTime deadline, LocalDateTime inicioLimite) {
        List<PlanningProcess> resultado = new ArrayList<>();

        EstrategiaPlanificacion estrategia = estrategias.get("BACKWARD");

        for (int i = 0; i < pedido.getQuantity(); i++) {
            PlanningProcess proceso = estrategia.ejecutar(
                    pedido.getProduct(), taller, agenda, deadline);

            if (proceso.getStart().isBefore(inicioLimite)) {
                throw new SchedulingException("El pedido no pudo planificarse en el plazo requerido", resultado.size());
            }

            proceso.setOrder(pedido);
            resultado.add(proceso);
        }
        return resultado;
    }

    private Integer positiveOrNull(int value) {
        return value > 0 ? value : null;
    }
}