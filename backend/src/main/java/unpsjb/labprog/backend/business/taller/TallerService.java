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
import unpsjb.labprog.backend.model.Pedido;
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

    public List<Taller> findAllConEquipos() {
        return repository.findAllConEquipos();
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

    @Transactional
    public Taller save(Taller workshop) {
        return repository.save(workshop);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }

    public List<Taller> obtenerPosiblesTalleres(List<TipoEquipo> types) {
        return repository.findPosiblesTalleresConEquipos(types, types.size());
    }

    public void validarSoporteEquipo(String code, List<TipoEquipo> types) {
        long matchingTypesCount = repository.contarTiposEquipoCoincidentes(code, types);
        if (matchingTypesCount != types.size())
            throw new BusinessException(
                    "El taller " + code + " no cuenta con los equipos necesarios para fabricar el producto");
    }

    public Taller obtenerTaller(String workshopCode, List<TipoEquipo> requiredTypes) {
        if (workshopCode != null) {
            Taller result = this.findByCode(workshopCode);
            this.validarSoporteEquipo(workshopCode, requiredTypes);
            return result;
        }

        List<Taller> result = this.obtenerPosiblesTalleres(requiredTypes);
        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    public List<Taller> filtrarTalleresPor(Pedido pedido, List<Taller> talleres) {
        List<Taller> result = new ArrayList<>();
        for (Taller t : talleres) {
            if (t.soportaEquipamiento(pedido.getProducto().requiredEquipmentTypes())) {
                result.add(t);
            }
        }
        return result;
    }

}
