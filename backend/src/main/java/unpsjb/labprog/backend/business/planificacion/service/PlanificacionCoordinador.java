package unpsjb.labprog.backend.business.planificacion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.business.planificacion.domain.Agenda;
import unpsjb.labprog.backend.business.planificacion.domain.AgendaFactory;
import unpsjb.labprog.backend.business.producto.ProductoService;
import unpsjb.labprog.backend.business.taller.TallerService;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;
import unpsjb.labprog.backend.model.Producto;
import unpsjb.labprog.backend.model.Taller;

@Service
public class PlanificacionCoordinador {

    private static final int HORIZONTE_PLANIFICACION_DIAS = 30;

    @Autowired
    private PlanificacionService planificacionService;

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

    @Transactional
    public ProcesoPlanificacion planificarProducto(PlanningRequestDTO request) {
        LocalDateTime inicio = request.getStartDate().toLocalDate().atStartOfDay();
        Producto producto = productoService.findByName(request.getProductName());

        Taller taller = tallerService.obtenerTaller(
                request.getWorkshopCode(), producto.requiredEquipmentTypes());

        if (taller == null)
            throw new BusinessException("No se encontró un taller con el equipamiento requerido para el producto");

        LocalDateTime finHorizonte = inicio.plusDays(HORIZONTE_PLANIFICACION_DIAS);
        Agenda agenda = fabricaAgenda.crearParaTaller(taller, inicio, finHorizonte);

        ProcesoPlanificacion result = planificador.planificarHaciaAdelanteEspecífico(producto, taller, agenda, inicio);
        return planificacionService.save(result);
    }

    @Transactional
    public List<ProcesoPlanificacion> planificarPedido(PlanningFromOrderRequestDTO request) {
        Pedido pedido = pedidoService.findById(request.getOrder().getId());
        pedido.validatePlannable();

        LocalDateTime inicioLimite = request.getStartDate().toLocalDate().atStartOfDay();
        LocalDateTime deadline = pedido.getFechaEntrega().atStartOfDay();

        List<Taller> talleres = tallerService
                .obtenerPosiblesTalleres(pedido.getProducto().requiredEquipmentTypes());

        if (talleres.isEmpty()) {
            pedido.markAsUnschedulable("No existen talleres con el equipamiento requerido", null);
            pedidoService.save(pedido);
            return List.of();
        }

        Map<Long, Agenda> agendasTaller = fabricaAgenda.crearParaTalleres(talleres, inicioLimite, deadline);

        List<ProcesoPlanificacion> resultado = planificador.planificarPedido(pedido, inicioLimite,
                talleres,
                agendasTaller);

        pedidoService.save(pedido);
        if (!resultado.isEmpty()) {
            planificacionService.saveAll(resultado);
        }

        return resultado;
    }

    @Transactional
    public List<ProcesoPlanificacion> planificarBatch(LocalDateTime tiempoEjecucion) {
        List<Pedido> pedidosPendientes = pedidoService.buscarPendientes();
        if (pedidosPendientes.isEmpty())
            return List.of();

        List<Taller> talleres = tallerService.findAllConEquipos();

        Map<Long, Agenda> agendasTaller = fabricaAgenda.crearParaTalleres(talleres, tiempoEjecucion,
                calcularDeadlineMaximo(pedidosPendientes));

        List<ProcesoPlanificacion> resultado = ejecutarPlanificacionBatch(pedidosPendientes, talleres, agendasTaller, tiempoEjecucion);

        pedidoService.saveAll(pedidosPendientes);
        if (!resultado.isEmpty()) {
            planificacionService.saveAll(resultado);
        }

        return resultado;
    }

    private LocalDateTime calcularDeadlineMaximo(List<Pedido> pedidos) {
        return pedidos.get(pedidos.size() - 1).getFechaEntrega().atStartOfDay();
    }

    private List<ProcesoPlanificacion> ejecutarPlanificacionBatch(
            List<Pedido> pedidosPendientes, List<Taller> talleres, Map<Long, Agenda> agendasTaller, LocalDateTime tiempoEjecucion) {
        List<ProcesoPlanificacion> resultado = new ArrayList<>();
        for (Pedido pedido : pedidosPendientes) {
            List<Taller> talleresAptos = tallerService.filtrarTalleresPor(pedido, talleres);

            if (!talleresAptos.isEmpty()) {
                List<ProcesoPlanificacion> procesosPedido = planificador.planificarPedido(
                        pedido, tiempoEjecucion, talleresAptos, agendasTaller);
                resultado.addAll(procesosPedido);
            } else {
                pedido.markAsUnschedulable("No existen talleres con el equipamiento requerido", null);
            }
        }
        return resultado;
    }
}
