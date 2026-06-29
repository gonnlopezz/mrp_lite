package unpsjb.labprog.backend.business.taller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        return planificacionService.findByWorkshop(workshopId);
    }

    @Transactional
    public Taller save(Taller workshop) {
        if (workshop.getId() != null) {
            Taller tallerExistente = repository.findById(workshop.getId().intValue()).orElse(null);
            if (tallerExistente != null)
                validarEliminacionDeEquipos(tallerExistente, workshop);
        }
        return repository.save(workshop);
    }

    private void validarEliminacionDeEquipos(Taller tallerExistente, Taller nuevo) {
        for (Equipo equipoExistente : tallerExistente.getEquipos()) {
            if (!sigueExistiendoEn(equipoExistente, nuevo.getEquipos()))
                validarEquipoSinPlanificaciones(equipoExistente);
        }
    }

    private boolean sigueExistiendoEn(Equipo equipo, Collection<Equipo> equipos) {
        return equipos.stream().anyMatch(e -> e.getId() != null && e.getId().equals(equipo.getId()));
    }

    private void validarEquipoSinPlanificaciones(Equipo equipo) {
        if (planificacionService.existsByEquipoId(equipo.getId())) {
            throw new BusinessException("No se puede eliminar el equipo " + equipo.getCodigo()
                    + " porque tiene planificaciones asociadas.");
        }
    }

    @Transactional
    public void delete(int id) {
        if (!planificacionService.findByWorkshop(id).isEmpty()) {
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

    public Optional<Taller> obtenerTaller(String workshopCode, List<TipoEquipo> requiredTypes) {
        if (workshopCode != null) {
            Taller resultado = this.findByCode(workshopCode);
            this.validarSoporteEquipo(workshopCode, requiredTypes);
            return Optional.of(resultado);
        }

        List<Taller> resultado = this.obtenerPosiblesTalleres(requiredTypes);
        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(resultado.get(0));
    }

    public List<Taller> filtrarTalleresPor(Pedido pedido, List<Taller> talleres) {
        return talleres.stream().filter(t -> t.soportaEquipamiento(pedido.getProducto().tiposDeEquipoRequeridos()))
                .toList();
    }

}
