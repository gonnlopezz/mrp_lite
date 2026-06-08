package unpsjb.labprog.backend.business.order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.planning.PlanningProcessRepository;
import unpsjb.labprog.backend.model.ManufacturingOrder;
import unpsjb.labprog.backend.model.OrderState;
import unpsjb.labprog.backend.model.PlanningProcess;

@Service
public class ManufacturingOrderService {
    @Autowired
    ManufacturingOrderRepository repository;

    @Autowired
    PlanningProcessRepository planningProcessRepository;

    public List<ManufacturingOrder> findAll() {
        List<ManufacturingOrder> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<ManufacturingOrder> findByPage(int page, int size) {
        return repository.findAllOrderedByStatePriority(PageRequest.of(page, size));
    }

    public Page<ManufacturingOrder> findByPageAndState(int page, int size, OrderState state) {
        return repository.findByStateOrderByDeliveryDateAsc(state, PageRequest.of(page, size, Sort.by("deliveryDate").ascending()));
    }

    public Page<ManufacturingOrder> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public ManufacturingOrder findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
    }

    public ManufacturingOrder findByCustomerCuitAndDeliveryDate(long cuit, LocalDate deliveryDate) {
        return repository.findByCustomerCuitAndDeliveryDate(cuit, deliveryDate);
    }

    public List<PlanningProcess> findPlanningProcesses(long orderId) {
        return planningProcessRepository.findByOrderId(orderId) ;
    }

    public List<ManufacturingOrder> findByStateOrderByDeliveryDateAsc(OrderState state) {
        return repository.findByStateOrderByDeliveryDateAsc(state);
    }

    @Transactional
    public ManufacturingOrder save(ManufacturingOrder order) {
        return repository.save(order);
    }

    @Transactional
    public List<ManufacturingOrder> saveAll(List<ManufacturingOrder> orders) {
        return repository.saveAll(orders);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

}
