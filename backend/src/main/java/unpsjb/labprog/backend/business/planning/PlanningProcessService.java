package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.product.ProductRepository;
import unpsjb.labprog.backend.business.workshop.WorkshopRepository;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.model.Equipment;
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
    ProductRepository productRepository;

    @Autowired
    WorkshopRepository workshopRepository;

    @Transactional
    public PlanningProcess save(PlanningRequestDTO request) {
        LocalDateTime normalizedStart = request.getStartDate().toLocalDate().atStartOfDay();
        PlanningProcess process = productPlanning(request.getProductName(), request.getWorkshopCode(), normalizedStart);
        return repository.save(process);
    }

    private PlanningProcess productPlanning(String productName, String workshopCode, LocalDateTime start) {
        PlanningProcess process = new PlanningProcess();

        Product product = productRepository.findByName(productName).orElse(null);
        Workshop workshop = workshopRepository.findByCode(workshopCode).orElse(null);

        Collection<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;
        Collection<Equipment> equipments = workshop.getEquipments();

        process.setStart(currentTime);

        for (Task t : product.getTasks()) {
            Equipment eq = equipments.stream().filter(e -> e.getType().equals(t.getType())).findFirst().orElse(null);
            
            LocalDateTime availableTime = getNextAvailableSlot(eq, currentTime);
            LocalDateTime end = availableTime.plusMinutes(t.getDuration());
            
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

    private LocalDateTime getNextAvailableSlot(Equipment equipment, LocalDateTime requestedTime) {
        if (equipment == null) return requestedTime;
        
        
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

    public PlanningProcess findById(long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
}