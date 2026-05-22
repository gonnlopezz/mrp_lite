package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        List<PlanningProcess> result = new ArrayList<>();
        Map<Long, LocalDateTime> equipmentFreeTime = new HashMap<>();

        for (int i = 0; i < order.getQuantity(); i++) {
            PlanningProcess unitProcess = productPlanningBackwards(product, finalDeliveryDate, equipmentFreeTime);
            unitProcess.setOrder(order);
            result.add(unitProcess);
        }

        order.setState(OrderState.PLANIFICADO);
        orderService.save(order);

        return (List<PlanningProcess>) repository.saveAll(result); 
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

    private PlanningProcess productPlanningBackwards(Product product, LocalDateTime deadline,
        Map<Long, LocalDateTime> equipmentFreeTime) {
        List<EquipmentType> requiredTypes = getRequiredEquipmentTypesFor(product);

        Workshop workshop = resolveWorkshop(null, requiredTypes);
        Collection<Equipment> equipments = workshop.getEquipments();

        List<Task> reversedTasks = new ArrayList<>(product.getTasks());
        Collections.reverse(reversedTasks);

        List<Planning> plannings = new LinkedList<>();

        LocalDateTime currentProductEnd = deadline;

        for (Task t : reversedTasks) {
            Equipment eq = getRequiredEquipmentFor(t, equipments);

            LocalDateTime eqAvailableUntil = equipmentFreeTime.getOrDefault(eq.getId(), deadline);

            LocalDateTime end = currentProductEnd.isBefore(eqAvailableUntil) ? currentProductEnd : eqAvailableUntil;
            LocalDateTime start = end.minusMinutes(calculateTaskDurationFor(t, eq));

            plannings.add(0, createPlanning(t, eq, start, end));

            currentProductEnd = start;

            equipmentFreeTime.put(eq.getId(), start);
        }

        return createPlanningProcess(plannings, deadline, currentProductEnd);

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
        List<EquipmentType> result = aProduct.getTasks().stream()
                .map(Task::getType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return result;
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
        long result = aTask.getDuration() / aEquipment.getCapacity();
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

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
}