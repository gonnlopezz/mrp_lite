package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.persistence.EntityNotFoundException;
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
    public PlanningProcess saveFromOrder(PlanningFromOrderRequestDTO request) {
        ManufacturingOrder order = orderService.findById(request.getOrder().getId());
        Product product = productService.findById(order.getProduct().getId());

        LocalDateTime deadline = order.getDeliveryDate().atStartOfDay();

        PlanningProcess result = productPlanningBackwards(product, deadline);
        return repository.save(result);
    }

    private PlanningProcess productPlanning(String productName, String workshopCode, LocalDateTime start) {

        PlanningProcess process = new PlanningProcess();
        Workshop workshop;
        Product product = productService.findByName(productName);

        List<EquipmentType> requiredTypes = product.getTasks().stream()
                .map(Task::getType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (workshopCode != null) {
            workshop = workshopService.findByCode(workshopCode);

            boolean canHandle = requiredTypes.stream()
                    .allMatch(type -> workshop.getEquipments().stream()
                            .anyMatch(e -> e.getType().equals(type)));

            if (!canHandle)
                throw new BusinessException("El taller no cuenta con los equipos necesarios para fabricar el producto");
        } else {
            workshop = workshopService.findByEquipmentTypes(requiredTypes, requiredTypes.size());
        }

        Collection<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;
        Collection<Equipment> equipments = workshop.getEquipments();

        process.setStart(currentTime);

        for (Task t : product.getTasks()) {
            Equipment eq = equipments.stream()
                    .filter(e -> e.getType().equals(t.getType()))
                    .findFirst().orElse(null);

            long taskDuration = t.getDuration() / (eq != null ? eq.getCapacity() : 1);
            LocalDateTime availableTime = getNextAvailableSlot(eq, currentTime);
            LocalDateTime end = availableTime.plusMinutes(taskDuration);

            Planning p = new Planning();
            p.setTask(t);
            p.setPeriod(new Period(availableTime, end, t.getDuration()));
            p.setEquipment(eq);
            plannings.add(p);

            currentTime = end;
        }

        process.setEndDate(currentTime);
        process.setPlannings(plannings);
        return process;
    }

    private PlanningProcess productPlanningBackwards(Product product, LocalDateTime deadline) {
        PlanningProcess process = new PlanningProcess();

        List<EquipmentType> requiredTypes = product.getTasks().stream()
                .map(Task::getType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Workshop workshop = workshopService.findByEquipmentTypes(requiredTypes, requiredTypes.size());
        Collection<Equipment> equipments = workshop.getEquipments();

        List<Task> reversedTasks = new ArrayList<>(product.getTasks());
        Collections.reverse(reversedTasks);
        
        Collection<Planning> plannings = new ArrayList<>();
        LocalDateTime currentEnd = deadline;

        for (Task t : reversedTasks) {
            Equipment eq = equipments.stream()
                    .filter(e -> e.getType().equals(t.getType()))
                    .findFirst().orElse(null);

            long taskDuration = t.getDuration() / (eq != null ? eq.getCapacity() : 1);

            LocalDateTime start = currentEnd.minusMinutes(taskDuration);

            Planning p = new Planning();
            p.setTask(t);
            p.setPeriod(new Period(start, currentEnd, t.getDuration()));
            p.setEquipment(eq);
            plannings.add(p);

            currentEnd = start;
        }

        List<Planning> orderedPlannings = plannings.stream()
                .sorted(Comparator.comparing(p -> p.getPeriod().getStart()))
                .collect(Collectors.toList());

        process.setStart(orderedPlannings.get(0).getPeriod().getStart());
        process.setEndDate(deadline);
        process.setPlannings(orderedPlannings);
        return process;
    }

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        if (equipment == null)
            return requestedTime;

        return repository.findMaxEndTimeForEquipment(equipment.getId())
                .map(maxEndTime -> maxEndTime.isAfter(requestedTime) ? maxEndTime : requestedTime)
                .orElse(requestedTime);
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