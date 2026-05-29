package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.product.ProductService;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.OrderState;
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
        Product product = productService.findById(order.getProduct().getId());

        LocalDateTime deliveryDate = order.getDeliveryDate().atStartOfDay();
        LocalDateTime requestedStart = request.getStartDate().toLocalDate().atStartOfDay();

        List<EquipmentType> requiredTypes = product.requiredEquipmentTypes();
        List<Workshop> workshops = workshopService.findAllByEquipmentTypes(requiredTypes);

        List<PlanningProcess> processes = searchValidPlanning(
                workshops, product, order, deliveryDate, requestedStart);

        order.setState(OrderState.PLANIFICADO);
        orderService.save(order);

        return processes;
    }

    private List<PlanningProcess> searchValidPlanning(
            List<Workshop> workshops, Product aProduct, ManufacturingOrder aOrder,
            LocalDateTime deliveryDate, LocalDateTime requestedStart) {

        for (Workshop workshop : workshops) {
            Optional<List<PlanningProcess>> result = simulateWorkshopPlanning(workshop, aProduct, aOrder, deliveryDate,
                    requestedStart);
            if (result.isPresent())
                return result.get();
        }

        throw new BusinessException(
                "No se encontró taller disponible para el pedido en el plazo requerido");
    }

    private Optional<List<PlanningProcess>> simulateWorkshopPlanning(
            Workshop aWorkshop, Product aProduct, ManufacturingOrder aOrder,
            LocalDateTime deadline, LocalDateTime requestedStart) {

        List<PlanningProcess> result = new ArrayList<>();
        Map<Long, LocalDateTime> freeTimeCache = new HashMap<>();

        for (int i = 0; i < aOrder.getQuantity(); i++) {
            PlanningProcess process = algorithm.scheduleBackwardFor(
                    aProduct, aWorkshop, deadline, freeTimeCache);
            process.setOrder(aOrder);

            if (process.getStart().isBefore(requestedStart))
                return Optional.empty();

            result.add(process);
        }

        return Optional.of(result);
    }

    public List<PlanningProcess> planBulkOrders(List<ManufacturingOrder> orders, LocalDateTime executionStart) {
        List<PlanningProcess> totalProcesses = new ArrayList<>();

        Map<Long, List<Period>> runtimeBusyCache = new HashMap<>();

        for (ManufacturingOrder order : orders) {
            Product product = productService.findById(order.getProduct().getId());
            List<EquipmentType> requiredTypes = product.requiredEquipmentTypes();
            List<Workshop> workshops = workshopService.findAllByEquipmentTypes(requiredTypes);

            boolean scheduledSuccessfully = false;

            for (Workshop workshop : workshops) {
                try {
                    List<PlanningProcess> orderProcesses = new ArrayList<>();
                    Map<Long, List<Period>> simulationCache = deepCopyCache(runtimeBusyCache);

                    for (int i = 0; i < order.getQuantity(); i++) {
                        LocalDateTime deadline = order.getDeliveryDate().atStartOfDay();

                        PlanningProcess process = algorithm.scheduleBackwardForBulk(
                                product, workshop, deadline, executionStart, simulationCache);
                        process.setOrder(order);
                        orderProcesses.add(process);
                    }

                    runtimeBusyCache = simulationCache;
                    totalProcesses.addAll(orderProcesses);
                    order.setState(OrderState.PLANIFICADO);
                    scheduledSuccessfully = true;
                    break;
                } catch (BusinessException e) {
                }
            }

            if (!scheduledSuccessfully) {
                order.setState(OrderState.NO_PLANIFICABLE);
            }
            orderService.save(order);
        }
        return totalProcesses;
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
}