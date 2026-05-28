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
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Product;
import unpsjb.labprog.backend.model.Workshop;
import unpsjb.labprog.backend.model.Task;

@Service
public class PlanningScheduler {

    @Autowired ProductService productService;
    @Autowired WorkshopService workshopService;
    @Autowired ManufacturingOrderService orderService;
    @Autowired PlanningAlgorithm algorithm;

    // ─── API PÚBLICA ──────────────────────────────────────────
    public PlanningProcess planForward(PlanningRequestDTO request) {
        LocalDateTime start = request.getStartDate().toLocalDate().atStartOfDay();
        Product product = productService.findByName(request.getProductName());
        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        Workshop workshop = resolveWorkshop(request.getWorkshopCode(), requiredTypes);

        return algorithm.scheduleForward(product, workshop, start);
    }

    public List<PlanningProcess> planBackward(PlanningFromOrderRequestDTO request) {
        ManufacturingOrder order = orderService.findById(request.getOrder().getId());
        Product product = productService.findById(order.getProduct().getId());

        LocalDateTime deliveryDate = order.getDeliveryDate().atStartOfDay();
        LocalDateTime requestedStart = request.getStartDate().toLocalDate().atStartOfDay();

        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        List<Workshop> workshops = workshopService.findAllByEquipmentTypes(
                requiredTypes, requiredTypes.size());

        List<PlanningProcess> processes = searchValidPlanning(
                workshops, product, order, deliveryDate, requestedStart);

        order.setState(OrderState.PLANIFICADO);
        orderService.save(order);

        return processes;
    }

    // ─── ORQUESTACIÓN INTERNA ─────────────────────────────────
    private List<PlanningProcess> searchValidPlanning(
            List<Workshop> workshops, Product product, ManufacturingOrder order,
            LocalDateTime deliveryDate, LocalDateTime requestedStart) {

        for (Workshop workshop : workshops) {
            Optional<List<PlanningProcess>> result =
                simulateWorkshopPlanning(workshop, product, order, deliveryDate, requestedStart);
            if (result.isPresent())
                return result.get();
        }

        throw new BusinessException(
            "No se encontró taller disponible para el pedido en el plazo requerido");
    }

    private Optional<List<PlanningProcess>> simulateWorkshopPlanning(
            Workshop workshop, Product product, ManufacturingOrder order,
            LocalDateTime deadline, LocalDateTime requestedStart) {

        List<PlanningProcess> result = new ArrayList<>();
        Map<Long, LocalDateTime> freeTimeCache = new HashMap<>();

        for (int i = 0; i < order.getQuantity(); i++) {
            PlanningProcess process = algorithm.scheduleBackwardsFor(
                product, workshop, deadline, freeTimeCache);
            process.setOrder(order);

            if (process.getStart().isBefore(requestedStart))
                return Optional.empty();

            result.add(process);
        }

        return Optional.of(result);
    }

    private Workshop resolveWorkshop(String workshopCode, List<EquipmentType> requiredTypes) {
        if (workshopCode != null) {
            Workshop result = workshopService.findByCode(workshopCode);
            workshopService.validateEquipmentSupport(workshopCode, requiredTypes);
            return result;
        }
        return workshopService.findByEquipmentTypes(requiredTypes, requiredTypes.size());
    }

    private List<EquipmentType> getRequiredEquipmentTypesFor(Product product) {
        List<EquipmentType> result = new ArrayList<>();
        for (Task task : product.getTasks()) {
            EquipmentType type = task.getType();
            if (type != null && !result.contains(type))
                result.add(type);
        }
        return result;
    }
}