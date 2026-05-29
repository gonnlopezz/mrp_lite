package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
            Equipment equipment = aWorkshop.findEquipmentForType(task.getType());
            LocalDateTime availableTime = getNextAvailableSlot(equipment, currentTime);
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
            Equipment aEquipment, LocalDateTime maxEnd,
            long durationMinutes, Map<Long, LocalDateTime> equipmentFreeTime) {

        LocalDateTime candidateEnd = maxEnd;

        List<Period> busyPeriods = new ArrayList<>();

        for (Planning p : aEquipment.getPlannings()) {
            busyPeriods.add(p.getPeriod());
        }

        if (equipmentFreeTime.containsKey(aEquipment.getId())) {
            LocalDateTime freeTimeLimit = equipmentFreeTime.get(aEquipment.getId());
            busyPeriods.add(new Period(freeTimeLimit, maxEnd.plusDays(1), 0));
        }

        busyPeriods.sort((p1, p2) -> p2.getEndDate().compareTo(p1.getEndDate()));

        for (Period p : busyPeriods) {
            LocalDateTime candidateStart = candidateEnd.minusMinutes(durationMinutes);

            if (candidateStart.isBefore(p.getEndDate()) && candidateEnd.isAfter(p.getStart())) {
                candidateEnd = p.getStart();
            }
        }

        return candidateEnd;
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
                    equipment, currentEnd, duration, intraUnitFreeTime, runtimeBusyCache
            );
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

    private LocalDateTime findBulkAvailableEndBackward(
            Equipment aEquipment, LocalDateTime maxEnd, long durationMinutes, 
            Map<Long, LocalDateTime> intraUnitFreeTime, Map<Long, List<Period>> runtimeBusyCache) {

        LocalDateTime candidateEnd = maxEnd;
        List<Period> busyPeriods = new ArrayList<>();

        for (Planning p : aEquipment.getPlannings()) {
            busyPeriods.add(p.getPeriod());
        }

        if (runtimeBusyCache.containsKey(aEquipment.getId())) {
            busyPeriods.addAll(runtimeBusyCache.get(aEquipment.getId()));
        }

        if (intraUnitFreeTime.containsKey(aEquipment.getId())) {
            LocalDateTime freeTimeLimit = intraUnitFreeTime.get(aEquipment.getId());
            busyPeriods.add(new Period(freeTimeLimit, maxEnd.plusDays(2), 0));
        }

        busyPeriods.sort((p1, p2) -> p2.getEndDate().compareTo(p1.getEndDate()));

        for (Period p : busyPeriods) {
            LocalDateTime candidateStart = candidateEnd.minusMinutes(durationMinutes);
            if (candidateStart.isBefore(p.getEndDate()) && candidateEnd.isAfter(p.getStart())) {
                candidateEnd = p.getStart();
            }
        }

        return candidateEnd;
    }

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        return repository.findMaxEndTimeForEquipment(equipment.getId())
                .map(max -> max.isAfter(requestedTime) ? max : requestedTime)
                .orElse(requestedTime);
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