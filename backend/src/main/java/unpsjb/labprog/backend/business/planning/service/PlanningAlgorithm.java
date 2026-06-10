package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
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
            LocalDateTime end = availableTime.plusMinutes(task.calculateDurationFor(equipment));

            plannings.add(new Planning(task, equipment, new Period(availableTime, end, task.getDuration())));
            currentTime = end;
        }

        return new PlanningProcess(plannings, plannings.get(0).getPeriod().getStart(), currentTime);
    }

    public PlanningProcess scheduleBackward(
            Product aProduct, Workshop aWorkshop, LocalDateTime deadline,
            Map<Long, LocalDateTime> intraUnitCache, Map<Long, List<Period>> crossUnitCache) {

        LinkedList<Planning> plannings = new LinkedList<>();
        LocalDateTime currentEnd = deadline;

        for (Task task : aProduct.getReverseTasks()) {
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            long duration = task.calculateDurationFor(equipment);

            LocalDateTime end = findAvailableEndBackward(equipment, currentEnd, duration, intraUnitCache,
                    crossUnitCache);
            LocalDateTime start = end.minusMinutes(duration);
            Period period = new Period(start, end, task.getDuration());

            plannings.addFirst(new Planning(task, equipment, period));

            crossUnitCache.computeIfAbsent(equipment.getId(), k -> new ArrayList<>()).add(period);
            intraUnitCache.put(equipment.getId(), start);
            currentEnd = start;
        }

        return new PlanningProcess(plannings, plannings.getFirst().getPeriod().getStart(), deadline);
    }

    private LocalDateTime findAvailableEndBackward(
            Equipment equipment, LocalDateTime maxEnd, long duration,
            Map<Long, LocalDateTime> intraUnitCache,
            Map<Long, List<Period>> crossUnitCache) {

        List<Period> busyPeriods = new ArrayList<>();

        for (Planning p : equipment.getPlannings())
            busyPeriods.add(p.getPeriod());

        busyPeriods.addAll(crossUnitCache.getOrDefault(equipment.getId(), List.of()));

        if (intraUnitCache.containsKey(equipment.getId())) {
            LocalDateTime nextTaskStart = intraUnitCache.get(equipment.getId());
            if (nextTaskStart.isBefore(maxEnd))
                maxEnd = nextTaskStart;

        }
        return resolveBackwardSlot(maxEnd, duration, busyPeriods);
    }

    private LocalDateTime resolveBackwardSlot(
            LocalDateTime maxEnd, long durationMinutes, List<Period> busyPeriods) {

        LocalDateTime result = maxEnd;
        List<Period> sortedBusyPeriods = new ArrayList<>(busyPeriods);
        sortedBusyPeriods.sort(Comparator.comparing(Period::getEndDate).reversed());

        for (Period busy : sortedBusyPeriods) {
            LocalDateTime candidateStart = result.minusMinutes(durationMinutes);
            if (candidateStart.isBefore(busy.getEndDate()) && result.isAfter(busy.getStart()))
                result = busy.getStart();
        }

        return result;
    }


}