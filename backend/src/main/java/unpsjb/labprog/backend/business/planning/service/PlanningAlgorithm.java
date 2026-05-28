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

    public PlanningProcess scheduleForward(
            Product aProduct, Workshop aWorkshop, LocalDateTime start) {

        List<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;

        for (Task task : aProduct.getTasks()) {
            Equipment equipment = getRequiredEquipmentFor(task, aWorkshop.getEquipments());
            LocalDateTime availableTime = getNextAvailableSlot(equipment, currentTime);
            LocalDateTime end = availableTime.plusMinutes(calculateTaskDurationFor(task, equipment));

            plannings.add(createPlanning(task, equipment, availableTime, end));
            currentTime = end;
        }

        return createPlanningProcess(plannings, start, currentTime);
    }

    public PlanningProcess scheduleBackwardFor(
            Product aProduct, Workshop aWorkshop,
            LocalDateTime deadline, Map<Long, LocalDateTime> freeTimeCache) {

        List<Task> reversedTasks = reverseTasksOf(aProduct);
        LinkedList<Planning> plannings = scheduleBackward(
                reversedTasks, aWorkshop.getEquipments(), deadline, freeTimeCache);

        LocalDateTime start = plannings.getFirst().getPeriod().getStart();

        return createPlanningProcess(
                plannings, start, deadline);
    }

    private LinkedList<Planning> scheduleBackward(
            List<Task> reversedTasks, Collection<Equipment> equipments,
            LocalDateTime deadline, Map<Long, LocalDateTime> freeTimeCache) {

        LinkedList<Planning> result = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : reversedTasks) {
            Equipment equipment = getRequiredEquipmentFor(task, equipments);
            long duration = calculateTaskDurationFor(task, equipment);

            LocalDateTime end = findAvailableEndBackward(equipment, currentEnd, duration, freeTimeCache);
            LocalDateTime start = end.minusMinutes(duration);

            result.addFirst(createPlanning(task, equipment, start, end));
            freeTimeCache.put(equipment.getId(), start);
            currentEnd = start;
        }

        return result;
    }

    private LocalDateTime findAvailableEndBackward(
            Equipment aEquipment, LocalDateTime maxEnd,
            long durationMinutes, Map<Long, LocalDateTime> freeTimeCache) {

        LocalDateTime result = maxEnd;
        if (freeTimeCache.containsKey(aEquipment.getId())
                && freeTimeCache.get(aEquipment.getId()).isBefore(result))
            result = freeTimeCache.get(aEquipment.getId());

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

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        return repository.findMaxEndTimeForEquipment(equipment.getId())
                .map(max -> max.isAfter(requestedTime) ? max : requestedTime)
                .orElse(requestedTime);
    }

    private Equipment getRequiredEquipmentFor(Task aTask, Collection<Equipment> equipments) {
        for (Equipment eq : equipments)
            if (eq.getType().equals(aTask.getType()))
                return eq;
        throw new BusinessException("Equipo no encontrado para la tarea: " + aTask.getName());
    }

    private long calculateTaskDurationFor(Task aTask, Equipment aEquipment) {
        return (long) Math.ceil((double) aTask.getDuration() / aEquipment.getCapacity());
    }

    private List<Task> reverseTasksOf(Product aProduct) {
        List<Task> result = new ArrayList<>(aProduct.getTasks());
        Collections.reverse(result);
        return result;
    }

    private Planning createPlanning(Task aTask, Equipment aEquipment, LocalDateTime start, LocalDateTime end) {
        Planning result = new Planning();
        result.setTask(aTask);
        result.setPeriod(new Period(start, end, aTask.getDuration()));
        result.setEquipment(aEquipment);
        return result;
    }

    private PlanningProcess createPlanningProcess(List<Planning> plannings, LocalDateTime start, LocalDateTime end) {
        PlanningProcess result = new PlanningProcess();
        result.setStart(start);
        result.setEndDate(end);
        result.setPlannings(plannings);
        return result;
    }
}