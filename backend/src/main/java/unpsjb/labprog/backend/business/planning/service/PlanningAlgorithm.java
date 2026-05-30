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

        LinkedList<Planning> plannings = scheduleBackward(
                aWorkshop, reverseTasksOf(aProduct), deadline,
                equipmentFreeTime, new HashMap<>());

        return new PlanningProcess(plannings, plannings.getFirst().getPeriod().getStart(), deadline);
    }

    public PlanningProcess scheduleBackwardForBulk(
            Product aProduct, Workshop aWorkshop, LocalDateTime deadline,
            LocalDateTime executionStart, Map<Long, List<Period>> runtimeBusyCache) {

        Map<Long, LocalDateTime> intraUnitFreeTime = new HashMap<>();
        LinkedList<Planning> plannings = scheduleBackward(
                aWorkshop, reverseTasksOf(aProduct), deadline,
                intraUnitFreeTime, runtimeBusyCache); 

        LocalDateTime start = plannings.getFirst().getPeriod().getStart();
        if (start.isBefore(executionStart))
            throw new BusinessException("La planificación excede el límite de inicio permitido.");

        return new PlanningProcess(plannings, start, deadline);
    }

    private LinkedList<Planning> scheduleBackward(
            Workshop aWorkshop, List<Task> reversedTasks, LocalDateTime deadline,
            Map<Long, LocalDateTime> freeTimeMap,
            Map<Long, List<Period>> runtimeCache) {

        LinkedList<Planning> result = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : reversedTasks) {
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            long duration = calculateTaskDurationFor(task, equipment);

            LocalDateTime end = findAvailableEndBackward(
                    equipment, currentEnd, duration, freeTimeMap, runtimeCache);
            LocalDateTime start = end.minusMinutes(duration);
            Period period = new Period(start, end, task.getDuration());

            result.addFirst(new Planning(task, equipment, period));
            runtimeCache.computeIfAbsent(equipment.getId(), k -> new ArrayList<>()).add(period);
            freeTimeMap.put(equipment.getId(), start);
            currentEnd = start;
        }

        return result;
    }

    private LocalDateTime findAvailableEndBackward(
            Equipment equipment, LocalDateTime maxEnd, long duration,
            Map<Long, LocalDateTime> freeTimeMap,
            Map<Long, List<Period>> runtimeCache) {

        List<Period> busyPeriods = new ArrayList<>();

        for (Planning p : equipment.getPlannings())
            busyPeriods.add(p.getPeriod());

        busyPeriods.addAll(runtimeCache.getOrDefault(equipment.getId(), List.of()));

        if (freeTimeMap.containsKey(equipment.getId()))
            busyPeriods.add(new Period(freeTimeMap.get(equipment.getId()), maxEnd.plusDays(2), 0));

        return resolveBackwardSlot(maxEnd, duration, busyPeriods);
    }

    private LocalDateTime resolveBackwardSlot(
            LocalDateTime maxEnd, long durationMinutes, List<Period> busyPeriods) {

        LocalDateTime result = maxEnd;
        List<Period> sorted = new ArrayList<>(busyPeriods);
        sorted.sort(Comparator.comparing(Period::getEndDate).reversed());

        for (Period busy : sorted) {
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
}