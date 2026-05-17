package unpsjb.labprog.backend.business.workshop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planning.PlanningProcessService;
import unpsjb.labprog.backend.model.Planning;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Workshop;

@Service
public class WorkshopService {
    @Autowired
    WorkshopRepository repository;

    @Autowired
    PlanningProcessService planningProcessService;

    public List<Workshop> findAll() {
        List<Workshop> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Workshop> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Workshop findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Workshop findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Taller no encontrado"));
    }

    public List<PlanningProcess> getPlanningProcesses(Integer workshopId) {
        findById(workshopId);
        return planningProcessService.findByWorkshop(workshopId);
    }

    @Transactional
    public Workshop save(Workshop workshop) {
        return repository.save(workshop);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }

}
