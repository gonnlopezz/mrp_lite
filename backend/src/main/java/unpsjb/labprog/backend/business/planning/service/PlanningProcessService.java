package unpsjb.labprog.backend.business.planning.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.order.ManufacturingOrderService;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.OrderState;
import unpsjb.labprog.backend.model.PlanningProcess;

@Service
public class PlanningProcessService {
    @Autowired
    PlanningProcessRepository repository;

    @Autowired
    PlanningScheduler scheduler;

    @Autowired
    ManufacturingOrderService orderService;

    @Transactional
    public PlanningProcess save(PlanningRequestDTO request) {
        return repository.save(scheduler.planForward(request));
    }

    @Transactional
    public List<PlanningProcess> saveFromOrder(PlanningFromOrderRequestDTO request) {
        return repository.saveAll(scheduler.planBackward(request));
    }

    public ManufacturingOrder findOrderById(long id) {
        return orderService.findById(id);
    }

    @Transactional
    public List<PlanningProcess> savePendingOrders(LocalDateTime executionTime) {
        List<ManufacturingOrder> pendingOrders = orderService.findByStateOrderByDeliveryDateAsc(OrderState.PENDIENTE);

        if (pendingOrders.isEmpty())
            return new ArrayList<>();

        List<PlanningProcess> processes = scheduler.planBulkOrders(pendingOrders, executionTime);
        repository.saveAll(processes);
        return processes;
    }

    public List<PlanningProcess> findAll() {
        List<PlanningProcess> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<PlanningProcess> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public List<PlanningProcess> findByWorkshop(Integer workshopId) {
        return repository.findAllByWorkshopId(workshopId);
    }

    public PlanningProcess findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Planificación no encontrada con id: " + id));
    }


    public List<PlanningProcess> findFiltered(Long workshopId, Long orderId) {
        if (workshopId == null && orderId == null)
            return this.findAll();

        return repository.findProcessesByFilters(workshopId, orderId);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
}