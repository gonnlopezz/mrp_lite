package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Equipment;
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

    // ─── PÚBLICO - llamado por PlanningScheduler ──────────────

    public PlanningProcess scheduleForward(
            Product product, Workshop workshop, LocalDateTime start) {

        List<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;

        for (Task task : product.getTasks()) {
            Equipment equipment = getRequiredEquipmentFor(task, workshop.getEquipments());
            LocalDateTime availableTime = getNextAvailableSlot(equipment, currentTime);
            LocalDateTime end = availableTime.plusMinutes(calculateTaskDurationFor(task, equipment));

            plannings.add(createPlanning(task, equipment, availableTime, end));
            currentTime = end;
        }

        return createPlanningProcess(plannings, start, currentTime);
    }

    public PlanningProcess scheduleBackwardsFor(
            Product product, Workshop workshop,
            LocalDateTime deadline, Map<Long, LocalDateTime> freeTimeCache) {

        List<Task> reversedTasks = reverseTasksOf(product);
        LinkedList<Planning> plannings = scheduleBackwards(
                reversedTasks, workshop.getEquipments(), deadline, freeTimeCache);

        return createPlanningProcess(
                plannings, plannings.getFirst().getPeriod().getStart(), deadline);
    }

    // ─── PRIVADO - detalles del algoritmo ────────────────────

    private LinkedList<Planning> scheduleBackwards(
            List<Task> reversedTasks, Collection<Equipment> equipments,
            LocalDateTime deadline, Map<Long, LocalDateTime> freeTimeCache) {

        LinkedList<Planning> result = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : reversedTasks) {
            Equipment equipment = getRequiredEquipmentFor(task, equipments);
            long duration = calculateTaskDurationFor(task, equipment);

            LocalDateTime end = findAvailableEndBackwards(equipment, currentEnd, duration, freeTimeCache);
            LocalDateTime start = end.minusMinutes(duration);

            result.addFirst(createPlanning(task, equipment, start, end));
            freeTimeCache.put(equipment.getId(), start);
            currentEnd = start;
        }

        return result;
    }

    private LocalDateTime findAvailableEndBackwards(
            Equipment equipment, LocalDateTime maxEnd,
            long durationMinutes, Map<Long, LocalDateTime> freeTimeCache) {

        LocalDateTime result = maxEnd;
        if (freeTimeCache.containsKey(equipment.getId())
                && freeTimeCache.get(equipment.getId()).isBefore(result))
            result = freeTimeCache.get(equipment.getId());

        List<Planning> existingPlannings = equipment.getPlannings();
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

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        return repository.findMaxEndTimeForEquipment(equipment.getId())
                .map(max -> max.isAfter(requestedTime) ? max : requestedTime)
                .orElse(requestedTime);
    }

    private Equipment getRequiredEquipmentFor(Task task, Collection<Equipment> equipments) {
        for (Equipment eq : equipments)
            if (eq.getType().equals(task.getType()))
                return eq;
        throw new BusinessException("Equipo no encontrado para la tarea: " + task.getName());
    }

    private List<Task> reverseTasksOf(Product product) {
        List<Task> result = new ArrayList<>(product.getTasks());
        Collections.reverse(result);
        return result;
    }

    private long calculateTaskDurationFor(Task task, Equipment equipment) {
        return (long) Math.ceil((double) task.getDuration() / equipment.getCapacity());
    }

    private Planning createPlanning(Task task, Equipment equipment,
            LocalDateTime start, LocalDateTime end) {
        Planning result = new Planning();
        result.setTask(task);
        result.setPeriod(new Period(start, end, task.getDuration()));
        result.setEquipment(equipment);
        return result;
    }

    private PlanningProcess createPlanningProcess(
            List<Planning> plannings, LocalDateTime start, LocalDateTime end) {
        PlanningProcess result = new PlanningProcess();
        result.setStart(start);
        result.setEndDate(end);
        result.setPlannings(plannings);
        return result;
    }
}