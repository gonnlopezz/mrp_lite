package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.product.ProductService;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.exception.SchedulingException;
import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.Period;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Product;
import unpsjb.labprog.backend.model.Workshop;

@Service
public class PlanningScheduler {

    @Autowired
    ProductService productService;
    @Autowired
    WorkshopService workshopService;
    @Autowired
    ManufacturingOrderService orderService;
    @Autowired
    PlanningAlgorithm algorithm;

    public PlanningProcess planForward(PlanningRequestDTO request) {
        LocalDateTime start = request.getStartDate().toLocalDate().atStartOfDay();
        Product product = productService.findByName(request.getProductName());
        List<EquipmentType> requiredTypes = product.requiredEquipmentTypes();
        Workshop workshop = resolveWorkshop(request.getWorkshopCode(), requiredTypes);
        return algorithm.scheduleForward(product, workshop, start);
    }

    public List<PlanningProcess> planBackward(PlanningFromOrderRequestDTO request) {
        ManufacturingOrder order = orderService.findById(request.getOrder().getId());
        order.validatePlannable();

        Product product = productService.findById(order.getProduct().getId());
        LocalDateTime deadline = order.getDeliveryDate().atStartOfDay();
        LocalDateTime requestedStart = request.getStartDate().toLocalDate().atStartOfDay();
        List<PlanningProcess> result = new ArrayList<>();

        try {
            List<Workshop> workshops = workshopService.findAllByEquipmentTypes(
                    product.requiredEquipmentTypes());
            result = scheduleBackwardOnFirstAvailableWorkshop(
                    workshops, product, order, deadline, requestedStart, new HashMap<>());
            order.markAsPlanned();
        } catch (SchedulingException e) {
            order.markAsUnschedulable(e.getMessage(), positiveOrNull(e.getSchedulableQuantity()));
        } catch (BusinessException e) {
            order.markAsUnschedulable(e.getMessage(), null);
        }

        orderService.save(order);
        return result;
    }

    // ── planBulkOrders: misma mejora ─────────────────────────────────────────

    public List<PlanningProcess> planBulkOrders(
            List<ManufacturingOrder> orders, LocalDateTime executionStart) {

        List<PlanningProcess> result = new ArrayList<>();
        Map<Long, List<Period>> runtimeCache = new HashMap<>();

        for (ManufacturingOrder order : orders) {
            Product product = productService.findById(order.getProduct().getId());
            try {
                List<Workshop> possibleWorkshops = workshopService.findAllByEquipmentTypes(
                        product.requiredEquipmentTypes());
                List<PlanningProcess> processes = scheduleBackwardOnFirstAvailableWorkshop(
                        possibleWorkshops, product, order,
                        order.getDeliveryDate().atStartOfDay(), executionStart, runtimeCache);
                result.addAll(processes);
                order.markAsPlanned();
            } catch (SchedulingException e) {
                order.markAsUnschedulable(e.getMessage(), positiveOrNull(e.getSchedulableQuantity()));
            } catch (BusinessException e) {
                order.markAsUnschedulable(e.getMessage(), null);
            }
            orderService.save(order);
        }
        return result;
    }


    private List<PlanningProcess> scheduleBackwardOnFirstAvailableWorkshop(
            List<Workshop> workshops, Product aProduct, ManufacturingOrder aOrder,
            LocalDateTime deadline, LocalDateTime startLimit,
            Map<Long, List<Period>> globalCache) {

        int bestSchedulableCount = 0;

        for (Workshop workshop : workshops) {
            Map<Long, List<Period>> simulationCache = deepCopyCache(globalCache);
            try {
                List<PlanningProcess> processes = scheduleUnitsBackward(
                        aOrder, aProduct, workshop, deadline, startLimit, simulationCache);
                globalCache.clear();
                globalCache.putAll(simulationCache);
                return processes;
            } catch (SchedulingException e) {
                bestSchedulableCount = Math.max(bestSchedulableCount, e.getSchedulableQuantity());
            } catch (BusinessException e) {
                // Taller sin el equipamiento necesario; ignorar y probar el siguiente
            }
        }

        throw new SchedulingException(
                "No se encontró taller disponible para el pedido en el plazo requerido.",
                bestSchedulableCount);
    }


    private List<PlanningProcess> scheduleUnitsBackward(
            ManufacturingOrder aOrder, Product aProduct, Workshop aWorkshop,
            LocalDateTime deadline, LocalDateTime startLimit,
            Map<Long, List<Period>> crossUnitCache) {

        List<PlanningProcess> result = new ArrayList<>();

        for (int i = 0; i < aOrder.getQuantity(); i++) {
            Map<Long, LocalDateTime> intraUnitCache = new HashMap<>();
            PlanningProcess process = algorithm.scheduleBackward(
                    aProduct, aWorkshop, deadline, intraUnitCache, crossUnitCache);
            process.setOrder(aOrder);

            if (process.getStart().isBefore(startLimit)) {
                throw new SchedulingException(
                        "El pedido no se puede planificar dentro del plazo requerido.",
                        result.size());
            }
            result.add(process);
        }
        return result;
    }

    private Workshop resolveWorkshop(String workshopCode, List<EquipmentType> requiredTypes) {
        if (workshopCode != null) {
            Workshop result = workshopService.findByCode(workshopCode);
            workshopService.validateEquipmentSupport(workshopCode, requiredTypes);
            return result;
        }
        return workshopService.findByEquipmentTypes(requiredTypes);
    }

    private Map<Long, List<Period>> deepCopyCache(Map<Long, List<Period>> original) {
        Map<Long, List<Period>> copy = new HashMap<>();
        for (Map.Entry<Long, List<Period>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    private Integer positiveOrNull(int value) {
        return value > 0 ? value : null;
    }
}