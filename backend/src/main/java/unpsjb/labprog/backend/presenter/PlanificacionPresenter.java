package unpsjb.labprog.backend.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.business.planificacion.service.PlanificacionCoordinador;
import unpsjb.labprog.backend.business.planificacion.service.PlanificacionService;
import unpsjb.labprog.backend.dto.PlanAllPendingRequestDTO;
import unpsjb.labprog.backend.dto.PlanningFromOrderRequestDTO;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.ProcesoPlanificacion;

@RestController
@RequestMapping("plannings")
public class PlanificacionPresenter {
    @Autowired
    PlanificacionService service;
    @Autowired
    PlanificacionCoordinador facade;
    @Autowired
    PedidoService pedidoService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> findAll() {
        return Response.ok(service.findAll());
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public ResponseEntity<Object> findByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Response.ok(service.findByPage(page, size));
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> findById(@PathVariable("id") long id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.GET)
    public ResponseEntity<Object> getPlanningsFiltered(
            @RequestParam(value = "workshopId", required = false) Long workshopId,
            @RequestParam(value = "orderId", required = false) Long orderId) {

        return Response.ok(service.findFiltered(workshopId, orderId));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> createPlanning(@RequestBody PlanningRequestDTO request) {
        if (request.getWorkshopCode() != null && request.getWorkshopCode().isBlank()) {
            request.setWorkshopCode(null);
        }
        try {
            return Response.ok(facade.planificarProducto(request), "Producto planificado con éxito");
        } catch (BusinessException e) {
            return Response.conflict(e.getMessage());
        } catch (EntityNotFoundException e) {
            return Response.notFound(e.getMessage());
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }

    }

    @PostMapping("/order")
    public ResponseEntity<Object> planFromOrder(@RequestBody PlanningFromOrderRequestDTO request) {
        try {
            List<ProcesoPlanificacion> result = facade.planificarPedido(request);

            if (result.isEmpty()) {
                Pedido failedOrder = pedidoService.findById(request.getOrder().getId());
                return Response.ok(failedOrder, "El pedido no pudo planificarse en el plazo requerido");
            }

            return Response.ok(result, "Pedido planificado con éxito");
        } catch (EntityNotFoundException e) {
            return Response.notFound(e.getMessage());
        } catch (BusinessException e) {
            return Response.conflict(e.getMessage());
        } catch (Exception e) {
            System.err.println("🚨 ERROR CRÍTICO EN TEST DE PLANIFICACIÓN: " + e.getMessage());
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }

    @PostMapping("/pending")
    public ResponseEntity<Object> planPendingOrders(@RequestBody PlanAllPendingRequestDTO request) {
        try {
            if (request.getStartDate() == null) {
                return Response.error("La fecha de inicio (startDate) es obligatoria.");
            }

            List<ProcesoPlanificacion> processes = facade.planificarBatch(request.getStartDate());

            return Response.ok(processes, "Pedidos pendientes planificados con éxito");
        } catch (Exception e) {
            return Response.error("Error al procesar la planificación masiva: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") long id) {
        service.delete(id);
        return Response.ok("Planificación id " + id + " eliminada con éxito.");
    }

}
