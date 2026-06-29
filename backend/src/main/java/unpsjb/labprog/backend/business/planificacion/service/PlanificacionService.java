package unpsjb.labprog.backend.business.planificacion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@Service
public class PlanificacionService {
    @Autowired
    private PlanificacionRepository repository;

    public List<ProcesoPlanificacion> findAll() {
        return repository.findAll();
    }

    public Page<ProcesoPlanificacion> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public List<ProcesoPlanificacion> findByWorkshop(Integer workshopId) {
        return repository.findAllByWorkshopId(workshopId);
    }

    public ProcesoPlanificacion findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Planificación no encontrada con id: " + id));
    }

    public List<ProcesoPlanificacion> findFiltered(Long workshopId, Long orderId) {
        if (workshopId == null && orderId == null)
            return this.findAll();

        return repository.findProcessesByFilters(workshopId, orderId);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

    public List<ProcesoPlanificacion> findByPedidoId(long pedidoId) {
        return repository.findByPedidoId(pedidoId);
    }

    public boolean existsByEquipoId(long equipoId) {
        return repository.existsByEquipoId(equipoId);
    }

    @Transactional
    public ProcesoPlanificacion save(ProcesoPlanificacion proceso) {
        return repository.save(proceso);
    }

    @Transactional
    public List<ProcesoPlanificacion> saveAll(List<ProcesoPlanificacion> procesos) {
        return repository.saveAll(procesos);
    }
}