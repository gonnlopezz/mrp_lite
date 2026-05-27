package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.business.product.ProductService;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Equipment;
import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.Period;
import unpsjb.labprog.backend.model.Planning;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Product;
import unpsjb.labprog.backend.model.Workshop;
import unpsjb.labprog.backend.model.Task;

@Service
public class PlanningAlgorithm {
    @Autowired
    PlanningProcessRepository repository;

    @Autowired
    ProductService productService;

    @Autowired
    WorkshopService workshopService;

    @Autowired
    ManufacturingOrderService orderService;

    /*
     * Planificación hacia adelante: se asignan las tareas en el orden dado,
     * buscando el próximo slot disponible para cada tarea
     */
    public PlanningProcess planningForward(String productName, String workshopCode, LocalDateTime start) {
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

    public List<PlanningProcess> planningBackward(
            ManufacturingOrder order,
            Product product,
            LocalDateTime deliveryDate,
            LocalDateTime requestedStart) {

        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        List<Workshop> availableWorkshops = workshopService.findAllByEquipmentTypes(
                requiredTypes, requiredTypes.size());

        List<PlanningProcess> result = searchValidPlanning(availableWorkshops, product, order, deliveryDate, requestedStart);
        return result;
    }

    public List<PlanningProcess> searchValidPlanning(
            List<Workshop> workshops, Product product, ManufacturingOrder order,
            LocalDateTime deliveryDate, LocalDateTime requestedStart) {

        for (Workshop workshop : workshops) {
            Optional<List<PlanningProcess>> result = simulateWorkshopPlanning(workshop, product, order, deliveryDate,
                    requestedStart);
            if (result.isPresent())
                return result.get();
        }

        throw new BusinessException(
                "No se encontró un taller con el equipamiento necesario para fabricar el producto dentro del plazo requerido");
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

    private PlanningProcess productPlanningBackwards(
            Product product, Workshop workshop,
            LocalDateTime deadline, Map<Long, LocalDateTime> equipmentFreeTime) {

        List<Task> reversedTasks = reverseTasksOf(product);
        LinkedList<Planning> plannings = scheduleBackwards(reversedTasks, workshop.getEquipments(), deadline,
                equipmentFreeTime);

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

    public List<EquipmentType> getRequiredEquipmentTypesFor(Product aProduct) {
        List<EquipmentType> result = new ArrayList<>();
        for (Task t : aProduct.getTasks()) {
            EquipmentType type = t.getType();
            if (!result.contains(type))
                result.add(type);
        }
        return result;
    }

    private Equipment getRequiredEquipmentFor(Task aTask, Collection<Equipment> equipments) {
        for (Equipment eq : equipments) {
            if (eq.getType().equals(aTask.getType()))
                return eq;
        }
        throw new BusinessException("Equipo no encontrado para la tarea: " + aTask.getName());
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

}