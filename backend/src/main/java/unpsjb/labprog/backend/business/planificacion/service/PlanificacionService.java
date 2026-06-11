package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.business.planificacion.PlanificacionRepository;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.AgendaFactory;
import unpsjb.labprog.backend.business.producto.ProductoService;
import unpsjb.labprog.backend.business.taller.TallerService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Producto;
import unpsjb.labprog.backend.model.Taller;

@Service
public class PlanificacionService {
    @Autowired
    private PlanificacionRepository repository;

    @Autowired
    private Planificador planificador;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private TallerService tallerService;

    @Autowired
    private AgendaFactory fabricaAgenda;

    @Autowired
    private ProductoService productoService;

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

    @Transactional
    public ProcesoPlanificacion planificarProducto(PlanningRequestDTO request) {
        LocalDateTime inicio = request.getStartDate().toLocalDate().atStartOfDay();
        Producto producto = productoService.findByName(request.getProductName());

        Taller taller = tallerService.resolverTaller(
                request.getWorkshopCode(), producto.requiredEquipmentTypes());

        LocalDateTime finHorizonte = inicio.plusDays(30);
        Agenda agenda = fabricaAgenda.crearParaTaller(taller, inicio, finHorizonte);

        ProcesoPlanificacion result = planificador.planificarHaciaAdelanteEspecífico(producto, taller, agenda, inicio);
        return repository.save(result);
    }

    @Transactional
    public List<ProcesoPlanificacion> planificarPedido(PlanningFromOrderRequestDTO request) {
        Pedido pedido = pedidoService.findById(request.getOrder().getId());
        pedido.validatePlannable();

        LocalDateTime inicioLimite = request.getStartDate().toLocalDate().atStartOfDay();
        LocalDateTime deadline = pedido.getFechaEntrega().atStartOfDay();

        List<Taller> talleresAptos = tallerService
                .findPossibleWorkshops(pedido.getProducto().requiredEquipmentTypes());

        if (talleresAptos.isEmpty()) {
            pedido.markAsUnschedulable("No existen talleres con el equipamiento requerido", null);
            pedidoService.save(pedido);
            return List.of();
        }

        Map<Long, Agenda> agendasInstanciadas = fabricaAgenda.crearParaTalleres(talleresAptos, inicioLimite, deadline);

        List<ProcesoPlanificacion> result = planificador.planificarPedidoEnTalleres(pedido, inicioLimite,
                talleresAptos,
                agendasInstanciadas);

        pedidoService.save(pedido);

        if (result.isEmpty())
            return result;

        return repository.saveAll(result);
    }

    @Transactional
    public List<ProcesoPlanificacion> planificarBatch(LocalDateTime tiempoEjecucion) {
        List<Pedido> pedidosPendientes = pedidoService.buscarPendientes();
        if (pedidosPendientes.isEmpty())
            return List.of();

        List<Taller> talleresOrdenados = tallerService.findAllConEquipos();

        LocalDateTime deadlineMaximo = pedidosPendientes.get(pedidosPendientes.size() - 1).getFechaEntrega()
                .atStartOfDay();
        Map<Long, Agenda> agendasInstanciadas = fabricaAgenda.crearParaTalleres(talleresOrdenados, tiempoEjecucion,
                deadlineMaximo);

        List<ProcesoPlanificacion> result = new ArrayList<>();

        for (Pedido pedido : pedidosPendientes) {
            List<Taller> talleresAptos = tallerService.filtrarTalleresPor(pedido, talleresOrdenados);

            if (!talleresAptos.isEmpty()) {
                List<ProcesoPlanificacion> procesosPedido = planificador.planificarPedidoEnTalleres(
                        pedido, tiempoEjecucion, talleresAptos, agendasInstanciadas);
                result.addAll(procesosPedido);
            } else {
                pedido.markAsUnschedulable("No existen talleres con el equipamiento requerido", null);
            }
        }

        pedidoService.saveAll(pedidosPendientes);
        return repository.saveAll(result);
    }

}