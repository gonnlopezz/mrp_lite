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
import java.util.Set;
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
        List<Workshop> availableWorkshops = workshopService.findAllByEquipmentTypes(requiredTypes, requiredTypes.size());

        List<PlanningProcess> finalProcesses = availableWorkshops.stream()
                .map(workshop -> simulateWorkshopPlanning(workshop, product, order, finalDeliveryDate, requestedStart))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new BusinessException("No se encontró un taller con el equipamiento y la ventana temporal requerida"));

        order.setState(OrderState.PLANIFICADO);
        orderService.save(order);

        return (List<PlanningProcess>) repository.saveAll(finalProcesses);
    }

    private Optional<List<PlanningProcess>> simulateWorkshopPlanning(
            Workshop workshop, 
            Product product, 
            ManufacturingOrder order, 
            LocalDateTime finalDeliveryDate, 
            LocalDateTime requestedStart) {
        
        List<PlanningProcess> candidateProcesses = new ArrayList<>();
        Map<Long, LocalDateTime> equipmentFreeTime = new HashMap<>();

        for (int i = 0; i < order.getQuantity(); i++) {
            PlanningProcess unitProcess = productPlanningBackwards(product, workshop, finalDeliveryDate, equipmentFreeTime);
            unitProcess.setOrder(order);

            if (unitProcess.getStart().isBefore(requestedStart)) {
                return Optional.empty();
            }
            candidateProcesses.add(unitProcess);
        }

        return Optional.of(candidateProcesses);
    }
    

    private PlanningProcess productPlanning(String productName, String workshopCode, LocalDateTime start) {
        Product product = productService.findByName(productName);
        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);
        Workshop workshop = resolveWorkshop(workshopCode, requiredTypes);

        List<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;
        Collection<Equipment> equipments = workshop.getEquipments();

        for (Task t : product.getTasks()) {
            Equipment eq = getRequiredEquipmentFor(t, equipments);
            LocalDateTime availableTime = getNextAvailableSlot(eq, currentTime);
            LocalDateTime end = availableTime.plusMinutes(calculateTaskDurationFor(t, eq));

            plannings.add(createPlanning(t, eq, availableTime, end));
            currentTime = end;
        }

        return createPlanningProcess(plannings, start, currentTime);
    }

    private PlanningProcess productPlanningBackwards(Product product, Workshop workshop, LocalDateTime deadline,
            Map<Long, LocalDateTime> equipmentFreeTime) {
        Collection<Equipment> equipments = workshop.getEquipments();

        List<Task> reversedTasks = new ArrayList<>(product.getTasks());
        Collections.reverse(reversedTasks);

        List<Planning> plannings = new LinkedList<>();
        LocalDateTime currentProductEnd = deadline;
        LocalDateTime overallStart = deadline;

        for (Task t : reversedTasks) {
            Equipment eq = getRequiredEquipmentFor(t, equipments);
            long durationMinutes = calculateTaskDurationFor(t, eq);


            LocalDateTime end = findAvailableEndBackwards(eq, currentProductEnd, durationMinutes, equipmentFreeTime);
            LocalDateTime start = end.minusMinutes(durationMinutes);

            plannings.add(0, createPlanning(t, eq, start, end));

            currentProductEnd = start;
            overallStart = start;

            equipmentFreeTime.put(eq.getId(), start);
        }

        return createPlanningProcess(plannings, overallStart, deadline);
    }

    private LocalDateTime findAvailableEndBackwards(Equipment eq, LocalDateTime maxEnd, long durationMinutes,
            Map<Long, LocalDateTime> equipmentFreeTime) {
        LocalDateTime targetEnd = maxEnd;
        if (equipmentFreeTime.containsKey(eq.getId()) && equipmentFreeTime.get(eq.getId()).isBefore(targetEnd)) {
            targetEnd = equipmentFreeTime.get(eq.getId());
        }

        List<Planning> existingPlannings = eq.getPlannings();
        if (existingPlannings == null || existingPlannings.isEmpty()) {
            return targetEnd;
        }

        boolean hasOverlap = true;
        while (hasOverlap) {
            hasOverlap = false;
            LocalDateTime targetStart = targetEnd.minusMinutes(durationMinutes);

            for (Planning p : existingPlannings) {
                if (p.getPeriod() == null)
                    continue;
                LocalDateTime bStart = p.getPeriod().getStart();
                LocalDateTime bEnd = p.getPeriod().getEndDate();

                if (targetStart.isBefore(bEnd) && targetEnd.isAfter(bStart)) {
                    targetEnd = bStart;
                    hasOverlap = true;
                    break;
                }
            }
        }
        return targetEnd;
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

    private Workshop resolveWorkshop(String workshopCode, List<EquipmentType> requiredTypes) {
        if (workshopCode != null) {
            Workshop workshop = workshopService.findByCode(workshopCode);
            Set<EquipmentType> availableTypes = workshop.getEquipments().stream()
                    .map(Equipment::getType)
                    .collect(Collectors.toSet());

            if (!availableTypes.containsAll(requiredTypes))
                throw new BusinessException("El taller " + workshop.getCode()
                        + " no cuenta con los equipos necesarios para fabricar el producto");

            return workshop;
        }

        List<Workshop> result = workshopService.findAllByEquipmentTypes(requiredTypes, requiredTypes.size());
        if (result.isEmpty())
            throw new BusinessException("No se encontró un taller con el equipamiento requerido para el producto");

        return result.get(0);
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
        Equipment result = equipments.stream()
                .filter(e -> e.getType().equals(aTask.getType()))
                .findFirst().orElse(null);

        if (result == null)
            throw new BusinessException("Equipo no encontrado para la tarea");

        return result;
    }

    private long calculateTaskDurationFor(Task aTask, Equipment aEquipment) {
        return aTask.getDuration() / aEquipment.getCapacity();
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