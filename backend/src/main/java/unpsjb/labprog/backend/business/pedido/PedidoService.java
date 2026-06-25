package unpsjb.labprog.backend.business.pedido;

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
import unpsjb.labprog.backend.business.planificacion.service.PlanificacionService;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.EstadoPedido;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@Service
public class PedidoService {
    @Autowired
    PedidoRepository repository;

    @Autowired
    PlanificacionService planificacionService;

    public List<Pedido> findAll() {
        List<Pedido> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Pedido> findByPage(int page, int size) {
        return repository.findAllOrderedByStatePriority(PageRequest.of(page, size));
    }

    public Page<Pedido> findByPageAndState(int page, int size, EstadoPedido state) {
        return repository.findByEstadoOrderByFechaEntregaAsc(state,
                PageRequest.of(page, size, Sort.by("fechaEntrega").ascending()));
    }

    public Page<Pedido> search(String term, EstadoPedido state, int page, int size) {
        if (state != null) {
            return repository.searchAndState(term, state, PageRequest.of(page, size));
        }
        return repository.search(term, PageRequest.of(page, size));
    }

    public Pedido findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
    }

    public Pedido findByCustomerCuitAndDeliveryDate(long cuit, LocalDate deliveryDate) {
        return repository.findByCustomerCuitAndDeliveryDate(cuit, deliveryDate);
    }

    public List<ProcesoPlanificacion> findPlanningProcesses(long orderId) {
        return planificacionService.findByPedidoId(orderId);
    }

    public List<Pedido> findByEstadoOrderByFechaEntregaAsc(EstadoPedido state) {
        return repository.findByEstadoOrderByFechaEntregaAsc(state);
    }

    @Transactional
    public Pedido save(Pedido order) {
        return repository.save(order);
    }

    @Transactional
    public List<Pedido> saveAll(List<Pedido> orders) {
        return repository.saveAll(orders);
    }

    @Transactional
    public void delete(long id) {
        Pedido pedido = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (pedido.getEstado() == EstadoPedido.PLANIFICADO) {
            throw new BusinessException("No se puede eliminar el pedido porque ya se encuentra planificado.");
        }
        if (pedido.getEstado() == EstadoPedido.FINALIZADO) {
            throw new BusinessException("No se puede eliminar el pedido porque ya se encuentra finalizado.");
        }
        if (!planificacionService.findByPedidoId(id).isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar el pedido porque ya cuenta con una planificación asociada.");
        }
        repository.deleteById(id);
    }

    public List<Pedido> buscarPendientes() {
        return repository.findByEstadoOrderByFechaEntregaAsc(EstadoPedido.PENDIENTE);
    }

}
