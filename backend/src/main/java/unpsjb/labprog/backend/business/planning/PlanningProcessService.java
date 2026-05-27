package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.product.ProductService;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Equipment;
import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.OrderState;
import unpsjb.labprog.backend.model.Period;
import unpsjb.labprog.backend.model.Planning;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Product;
import unpsjb.labprog.backend.model.Workshop;
import unpsjb.labprog.backend.model.Task;

@Service
public class PlanningProcessService {
    @Autowired
    PlanningProcessRepository repository;

    @Autowired
    ProductService productService;

    @Autowired
    WorkshopService workshopService;

    @Autowired
    ManufacturingOrderService orderService;

    @Transactional
    public PlanningProcess save(PlanningRequestDTO request) {
        LocalDateTime normalizedStart = request.getStartDate().toLocalDate().atStartOfDay();
        PlanningProcess process = productPlanning(request.getProductName(), request.getWorkshopCode(), normalizedStart);
        return repository.save(process);
    }

    @Transactional
    public List<PlanningProcess> saveFromOrder(PlanningFromOrderRequestDTO request) {
        ManufacturingOrder order = orderService.findById(request.getOrder().getId());
        Product product = productService.findById(order.getProduct().getId());

        LocalDateTime finalDeliveryDate = order.getDeliveryDate().atStartOfDay();
        LocalDateTime requestedStart = request.getStartDate().toLocalDate().atStartOfDay();

        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        List<Workshop> availableWorkshops = workshopService.findAllByEquipmentTypes(requiredTypes,
                requiredTypes.size());

        List<PlanningProcess> finalProcesses = searchValidPlanning(availableWorkshops, product, order,
                finalDeliveryDate, requestedStart);
        order.setState(OrderState.PLANIFICADO);
        orderService.save(order);

        return (List<PlanningProcess>) repository.saveAll(finalProcesses);
    }

    private PlanningProcess productPlanning(String productName, String workshopCode, LocalDateTime start) {
        Product product = productService.findByName(productName);
        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        Workshop workshop = resolveWorkshop(workshopCode, requiredTypes);

        List<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;
        Collection<Equipment> equipments = workshop.getEquipments();

        for (Task t : product.getTasks()) {
            Equipment equipment = getRequiredEquipmentFor(t, equipments);
            LocalDateTime availableTime = getNextAvailableSlot(equipment, currentTime);
            LocalDateTime end = availableTime.plusMinutes(calculateTaskDurationFor(t, equipment));

            plannings.add(createPlanning(t, equipment, availableTime, end));
            currentTime = end;
        }

        return createPlanningProcess(plannings, start, currentTime);
    }

    private PlanningProcess productPlanningBackwards(
            Product product, Workshop workshop,
            LocalDateTime deadline, Map<Long, LocalDateTime> equipmentFreeTime) {
    
        List<Task> reversedTasks = reverseTasksOf(product);
        LinkedList<Planning> plannings = scheduleBackwards(
                reversedTasks, workshop.getEquipments(), deadline, equipmentFreeTime);

        LocalDateTime overallStart = plannings.getFirst().getPeriod().getStart();
        return createPlanningProcess(plannings, overallStart, deadline);
    }

    private LinkedList<Planning> scheduleBackwards(
            List<Task> reversedTasks, Collection<Equipment> equipments,
            LocalDateTime deadline, Map<Long, LocalDateTime> equipmentFreeTime) {

        LinkedList<Planning> result = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : reversedTasks) {
            Equipment equipment = getRequiredEquipmentFor(task, equipments);
            long duration = calculateTaskDurationFor(task, equipment);

            LocalDateTime end = findAvailableEndBackwards(equipment, currentEnd, duration, equipmentFreeTime);
            LocalDateTime start = end.minusMinutes(duration);

            result.addFirst(createPlanning(task, equipment, start, end));
            equipmentFreeTime.put(equipment.getId(), start);
            currentEnd = start;
        }

        return result;
    }

    private LocalDateTime findAvailableEndBackwards(Equipment aEquipment, LocalDateTime maxEnd, long durationMinutes,
            Map<Long, LocalDateTime> equipmentFreeTime) {

        LocalDateTime result = maxEnd;
        if (equipmentFreeTime.containsKey(aEquipment.getId())
                && equipmentFreeTime.get(aEquipment.getId()).isBefore(result))
            result = equipmentFreeTime.get(aEquipment.getId());

        List<Planning> existingPlannings = aEquipment.getPlannings();
        if (existingPlannings == null || existingPlannings.isEmpty())
            return result;

        for (Planning p : existingPlannings) {
            LocalDateTime targetStart = result.minusMinutes(durationMinutes);
            LocalDateTime bStart = p.getPeriod().getStart();
            LocalDateTime bEnd = p.getPeriod().getEndDate();

            if (targetStart.isBefore(bEnd) && result.isAfter(bStart))
                result = bStart;
        }

        return result;
    }

    private List<PlanningProcess> searchValidPlanning(
            List<Workshop> workshops, Product product, ManufacturingOrder order,
            LocalDateTime deliveryDate, LocalDateTime requestedStart) {
        return workshops.stream()
                .flatMap(w -> simulateWorkshopPlanning(w, product, order, deliveryDate, requestedStart).stream())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No se encontró un taller con el equipamiento necesario para fabricar el producto dentro del plazo requerido"));
    }

    private Optional<List<PlanningProcess>> simulateWorkshopPlanning(
            Workshop aWorkshop, Product aProduct, ManufacturingOrder order,
            LocalDateTime finalDeliveryDate, LocalDateTime requestedStart) {

        List<PlanningProcess> result = new ArrayList<>();
        Map<Long, LocalDateTime> equipmentFreeTime = new HashMap<>();

        for (int i = 0; i < order.getQuantity(); i++) {
            PlanningProcess process = productPlanningBackwards(aProduct, aWorkshop, finalDeliveryDate,
                    equipmentFreeTime);
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

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        if (equipment == null)
            return requestedTime;

        return repository.findMaxEndTimeForEquipment(equipment.getId())
                .map(maxEndTime -> maxEndTime.isAfter(requestedTime) ? maxEndTime : requestedTime)
                .orElse(requestedTime);
    }

    private List<EquipmentType> getRequiredEquipmentTypesFor(Product aProduct) {
        return aProduct.getTasks().stream()
                .map(Task::getType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private Equipment getRequiredEquipmentFor(Task aTask, Collection<Equipment> equipments) {
        return equipments.stream()
                .filter(e -> e.getType().equals(aTask.getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Equipo no encontrado para la tarea: " + aTask.getName()));
    }

    private List<Task> reverseTasksOf(Product product) {
        List<Task> result = new ArrayList<>(product.getTasks());
        Collections.reverse(result);
        return result;
    }

    private PlanningProcess createPlanningProcess(List<Planning> plannings, LocalDateTime start, LocalDateTime end) {
        PlanningProcess result = new PlanningProcess();
        result.setStart(start);
        result.setEndDate(end);
        result.setPlannings(plannings);
        return result;
    }

    private Planning createPlanning(Task aTask, Equipment aEquipment, LocalDateTime startTime, LocalDateTime endTime) {
        Planning result = new Planning();
        result.setTask(aTask);
        result.setPeriod(new Period(startTime, endTime, aTask.getDuration()));
        result.setEquipment(aEquipment);
        return result;
    }

    private long calculateTaskDurationFor(Task aTask, Equipment aEquipment) {
        long result = (long) Math.ceil((double) aTask.getDuration() / aEquipment.getCapacity());
        return result;
    }

    public List<PlanningProcess> findAll() {
        List<PlanningProcess> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<PlanningProcess> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public List<PlanningProcess> findByWorkshop(Integer workshopId) {
        return repository.findAllByWorkshopId(workshopId);
    }

    public PlanningProcess findById(long id) {
        return repository.findById(id).orElse(null);
    }

    public List<PlanningProcess> findFiltered(Long workshopId, Long orderId) {
        if (workshopId == null && orderId == null)
            return this.findAll();

        return repository.findProcessesByFilters(workshopId, orderId);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
}