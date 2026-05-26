package unpsjb.labprog.backend.business.workshop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.EquipmentType;
import unpsjb.labprog.backend.model.PlanningProcess;
import unpsjb.labprog.backend.model.Workshop;

@Service
public class WorkshopService {
    @Autowired
    WorkshopRepository repository;

    @Autowired
    PlanningProcessRepository planningProcessRepository;

    public List<Workshop> findAll() {
        List<Workshop> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Workshop> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Workshop> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public Workshop findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Workshop findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Taller no encontrado"));
    }

    public List<PlanningProcess> getPlanningProcesses(Integer workshopId) {
        findById(workshopId);
        return planningProcessRepository.findAllByWorkshopId(workshopId);
    }

    public Workshop findByEquipmentTypes(List<EquipmentType> types, int count) {
        return repository.findByEquipmentTypes(types, count).orElseThrow(
                () -> new BusinessException("No se encontró un taller con el equipamiento requerido para el producto"));
    }

    public List<Workshop> findAllByEquipmentTypes(List<EquipmentType> types, int count) {
        List<Workshop> result = repository.findAllByEquipmentTypes(types, count);
        if (result.isEmpty()) throw new BusinessException("No se encontró un taller con el equipamiento requerido para el producto");
        return result;
    }

    public void validateEquipmentSupport(String code, List<EquipmentType> types) {
        long matchingTypesCount = repository.countMatchingEquipmentTypes(code, types);
        if(matchingTypesCount != types.size()) throw new BusinessException("El taller " + code + " no cuenta con los equipos necesarios para fabricar el producto");
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
