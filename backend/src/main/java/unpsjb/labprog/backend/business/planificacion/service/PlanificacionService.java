package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.EstadoPedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@Service
public class PlanificacionService {
    @Autowired
    PlanificacionRepository repository;

    @Autowired
    Planificador scheduler;

    @Autowired
    PedidoService orderService;

    @Transactional
    public ProcesoPlanificacion save(PlanningRequestDTO request) {
        return repository.save(scheduler.planForward(request));
    }

    @Transactional
    public List<ProcesoPlanificacion> saveFromOrder(PlanningFromOrderRequestDTO request) {
        return repository.saveAll(scheduler.planBackward(request));
    }

    public Pedido findOrderById(long id) {
        return orderService.findById(id);
    }

    @Transactional
    public List<ProcesoPlanificacion> savePendingOrders(LocalDateTime executionTime) {
        List<Pedido> pendingOrders = orderService.findByEstadoOrderByFechaEntregaAsc(EstadoPedido.PENDIENTE);

        if (pendingOrders.isEmpty())
            return new ArrayList<>();

        List<ProcesoPlanificacion> processes = scheduler.planBulkOrders(pendingOrders, executionTime);
        repository.saveAll(processes);
        return processes;
    }

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
}