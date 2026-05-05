package unpsjb.labprog.backend.business.planning;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.product.ProductRepository;
import unpsjb.labprog.backend.business.workshop.WorkshopRepository;
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
    public PlanningProcess save(int productId, int workshopId, LocalDateTime start) {
        LocalDateTime normalizedStart = start.toLocalDate().atStartOfDay();
        PlanningProcess process = productPlanning(productId, workshopId, normalizedStart);
        return repository.save(process);
    }

    private PlanningProcess productPlanning(int productId, int workshopId, LocalDateTime start) {
        PlanningProcess process = new PlanningProcess();

        Product product = productRepository.findById(productId).orElse(null);
        Workshop workshop = workshopRepository.findById(workshopId).orElse(null);

        Collection<Planning> plannings = new ArrayList<>();
        LocalDateTime currentTime = start;
        Collection<Equipment> equipments = workshop.getEquipments();

        process.setStart(currentTime);

        for (Task t : product.getTasks()) {
            Planning p = new Planning();
            p.setTask(t);
            LocalDateTime end = start.plusMinutes(t.getDuration())  ;
            p.setPeriod(new Period(currentTime, end, t.getDuration()));
            currentTime = currentTime.plusMinutes(t.getDuration());
            
            Equipment eq = equipments.stream().filter(e -> e.getType().equals(t.getType())).findFirst().orElse(null);

            p.setEquipment(eq);
            plannings.add(p);
        }

        process.setEnd(currentTime);
        process.setPlannings(plannings);
        return process;
    }
}
