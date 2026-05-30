package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
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

    public PlanningProcess scheduleForward(
            Product aProduct, Workshop aWorkshop, LocalDateTime start) {

        List<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;

        for (Task task : aProduct.getTasks()) {
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            LocalDateTime availableTime = equipment.firstAvailableSlotAfter(currentTime);
            LocalDateTime end = availableTime.plusMinutes(calculateTaskDurationFor(task, equipment));

            plannings.add(new Planning(task, equipment, new Period(availableTime, end, task.getDuration())));
            currentTime = end;
        }

        return new PlanningProcess(plannings, start, currentTime);
    }

    public PlanningProcess scheduleBackwardFor(
            Product aProduct, Workshop aWorkshop,
            LocalDateTime deadline, Map<Long, LocalDateTime> equipmentFreeTime) {

        List<Task> reversedTasks = reverseTasksOf(aProduct);
        LinkedList<Planning> plannings = scheduleBackward(
                aWorkshop, reversedTasks, deadline, equipmentFreeTime);

        LocalDateTime start = plannings.getFirst().getPeriod().getStart();

        return new PlanningProcess(
                plannings, start, deadline);
    }

    private LinkedList<Planning> scheduleBackward(
            Workshop aWorkshop,
            List<Task> reversedTasks,
            LocalDateTime deadline, Map<Long, LocalDateTime> equipmentFreeTime) {

        LinkedList<Planning> result = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : reversedTasks) {
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            long duration = calculateTaskDurationFor(task, equipment);

            LocalDateTime end = findAvailableEndBackward(equipment, currentEnd, duration, equipmentFreeTime);
            LocalDateTime start = end.minusMinutes(duration);

            result.addFirst(new Planning(task, equipment, new Period(start, end, task.getDuration())));
            equipmentFreeTime.put(equipment.getId(), start);
            currentEnd = start;
        }

        return result;
    }

    private LocalDateTime findAvailableEndBackward(
            Equipment equipment, LocalDateTime maxEnd,
            long duration, Map<Long, LocalDateTime> equipmentFreeTime) {

        List<Period> busyPeriods = new ArrayList<>();
        for (Planning p : equipment.getPlannings())
            busyPeriods.add(p.getPeriod());
        if (equipmentFreeTime.containsKey(equipment.getId())) {
            busyPeriods.add(new Period(
                    equipmentFreeTime.get(equipment.getId()), maxEnd.plusDays(2), 0));
        }

        return resolveBackwardSlot(maxEnd, duration, busyPeriods);
    }

    private LocalDateTime findBulkAvailableEndBackward(
            Equipment equipment, LocalDateTime maxEnd, long duration,
            Map<Long, LocalDateTime> intraUnitFreeTime,
            Map<Long, List<Period>> runtimeBusyCache) {

        List<Period> busyPeriods = new ArrayList<>();
        for (Planning p : equipment.getPlannings())
            busyPeriods.add(p.getPeriod());
        if (runtimeBusyCache.containsKey(equipment.getId()))
            busyPeriods.addAll(runtimeBusyCache.get(equipment.getId()));
        if (intraUnitFreeTime.containsKey(equipment.getId())) {
            busyPeriods.add(new Period(
                    intraUnitFreeTime.get(equipment.getId()), maxEnd.plusDays(2), 0));
        }

        return resolveBackwardSlot(maxEnd, duration, busyPeriods);
    }

    public PlanningProcess scheduleBackwardForBulk(
            Product aProduct, Workshop aWorkshop, LocalDateTime deadline,
            LocalDateTime executionStart, Map<Long, List<Period>> runtimeBusyCache) {

        List<Task> reversedTasks = new ArrayList<>(aProduct.getTasks());
        Collections.reverse(reversedTasks);

        LinkedList<Planning> plannings = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        Map<Long, LocalDateTime> intraUnitFreeTime = new HashMap<>();

        for (Task task : reversedTasks) {
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            long duration = (long) Math.ceil((double) task.getDuration() / equipment.getCapacity());

            LocalDateTime end = findBulkAvailableEndBackward(
                    equipment, currentEnd, duration, intraUnitFreeTime, runtimeBusyCache);
            LocalDateTime start = end.minusMinutes(duration);

            if (start.isBefore(executionStart)) {
                throw new BusinessException("La planificación excede el límite de inicio permitido.");
            }

            Period allocatedPeriod = new Period(start, end, task.getDuration());
            plannings.addFirst(new Planning(task, equipment, allocatedPeriod));

            runtimeBusyCache.computeIfAbsent(equipment.getId(), k -> new ArrayList<>()).add(allocatedPeriod);
            intraUnitFreeTime.put(equipment.getId(), start);

            currentEnd = start;
        }

        return new PlanningProcess(plannings, plannings.getFirst().getPeriod().getStart(), deadline);
    }

    private LocalDateTime resolveBackwardSlot(
            LocalDateTime maxEnd, long durationMinutes, List<Period> busyPeriods) {

        LocalDateTime result = maxEnd;
        List<Period> sortedPeriods = reverseBusyPeriods(busyPeriods); 
        sortedPeriods.sort(Comparator.comparing(Period::getEndDate).reversed());

        for (Period busy : sortedPeriods) {
            LocalDateTime candidateStart = result.minusMinutes(durationMinutes);
            if (candidateStart.isBefore(busy.getEndDate()) && result.isAfter(busy.getStart()))
                result = busy.getStart();
        }

        return result;
    }

    private long calculateTaskDurationFor(Task aTask, Equipment aEquipment) {
        return (long) Math.ceil((double) aTask.getDuration() / aEquipment.getCapacity());
    }

    private List<Task> reverseTasksOf(Product aProduct) {
        List<Task> result = new ArrayList<>(aProduct.getTasks());
        Collections.reverse(result);
        return result;
    }

    private List<Period> reverseBusyPeriods(List<Period> busyPeriods) {
        List<Period> result = new ArrayList<>(busyPeriods);
        Collections.reverse(result);
        return result;
    }

}