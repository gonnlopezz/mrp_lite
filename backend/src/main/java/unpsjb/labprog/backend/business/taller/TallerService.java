package unpsjb.labprog.backend.business.taller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.TipoEquipo;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Taller;

@Service
public class TallerService {
    @Autowired
    TallerRepository repository;

    @Autowired
    PlanificacionRepository planningProcessRepository;

    public List<Taller> findAll() {
        List<Taller> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Taller> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Taller> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public Taller findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Taller findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Taller no encontrado"));
    }

    public List<ProcesoPlanificacion> obtenerProcesosPlanificacion(Integer workshopId) {
        findById(workshopId);
        return planningProcessRepository.findAllByWorkshopId(workshopId);
    }

    public List<Taller> findPossibleWorkshops(List<TipoEquipo> types) {
        List<Taller> result = repository.findAllByTiposEquipo(types, types.size());
        if (result.isEmpty())
            throw new BusinessException("No se encontró un taller con el equipamiento requerido para el producto");
        return result;
    }

    public void validarSoporteEquipo(String code, List<TipoEquipo> types) {
        long matchingTypesCount = repository.contarTiposEquipoCoincidentes(code, types);
        if (matchingTypesCount != types.size())
            throw new BusinessException(
                    "El taller " + code + " no cuenta con los equipos necesarios para fabricar el producto");
    }

    public Taller resolverTaller(String workshopCode, List<TipoEquipo> requiredTypes) {
        if (workshopCode != null) {
            Taller result = this.findByCode(workshopCode);
            this.validarSoporteEquipo(workshopCode, requiredTypes);
            return result;
        }
        return this.findPossibleWorkshops(requiredTypes).get(0);
    }

    @Transactional
    public Taller save(Taller workshop) {
        return repository.save(workshop);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }

}
