package unpsjb.labprog.backend.business.taller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planificacion.service.PlanificacionService;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.TipoEquipo;
import unpsjb.labprog.backend.model.Equipo;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Taller;

@Service
public class TallerService {
    @Autowired
    TallerRepository repository;

    @Autowired
    PlanificacionService planificacionService;

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
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Taller no encontrado"));
    }

    public Taller findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Taller no encontrado"));
    }

    public List<ProcesoPlanificacion> obtenerProcesosPlanificacion(Integer workshopId) {
        findById(workshopId);
        return planificacionService.findAllByWorkshopId(workshopId);
    }

    @Transactional
    public Taller save(Taller workshop) {
        if (workshop.getId() != null) {
            Taller existing = repository.findById(workshop.getId().intValue()).orElse(null);
            if (existing != null) {
                for (Equipo existingEquipo : existing.getEquipos()) {
                    boolean stillExists = false;
                    for (Equipo newEquipo : workshop.getEquipos()) {
                        if (newEquipo.getId() != null && newEquipo.getId().equals(existingEquipo.getId())) {
                            stillExists = true;
                            break;
                        }
                    }
                    if (!stillExists) {
                        if (planificacionService.existsByEquipoId(existingEquipo.getId())) {
                            throw new BusinessException("No se puede eliminar el equipo " + existingEquipo.getCodigo()
                                    + " porque tiene planificaciones asociadas.");
                        }
                    }
                }
            }
        }
        return repository.save(workshop);
    }

    @Transactional
    public void delete(int id) {
        if (!planificacionService.findAllByWorkshopId(id).isEmpty()) {
            throw new BusinessException("No se puede eliminar el taller porque tiene planificaciones asociadas.");
        }
        repository.deleteById(id);
    }

    public List<Taller> obtenerPosiblesTalleres(List<TipoEquipo> types) {
        return repository.findPosiblesTalleresConEquipos(types, types.size());
    }

    public void validarSoporteEquipo(String code, List<TipoEquipo> types) {
        Taller taller = findByCode(code);
        if (!taller.soportaEquipamiento(types)) {
            throw new BusinessException(
                    "El taller " + code + " no cuenta con los equipos necesarios para fabricar el producto");
        }
    }

    public Taller obtenerTaller(String workshopCode, List<TipoEquipo> requiredTypes) {
        if (workshopCode != null) {
            Taller resultado = this.findByCode(workshopCode);
            this.validarSoporteEquipo(workshopCode, requiredTypes);
            return resultado;
        }

        List<Taller> resultado = this.obtenerPosiblesTalleres(requiredTypes);
        if (resultado.isEmpty()) {
            return null;
        }

        return resultado.get(0);
    }

    public List<Taller> filtrarTalleresPor(Pedido pedido, List<Taller> talleres) {
        List<Taller> resultado = new ArrayList<>();
        for (Taller t : talleres) {
            if (t.soportaEquipamiento(pedido.getProducto().tiposDeEquipoRequeridos())) {
                resultado.add(t);
            }
        }
        return resultado;
    }

}
