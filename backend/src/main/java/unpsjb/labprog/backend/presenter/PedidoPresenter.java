package unpsjb.labprog.backend.presenter;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.pedido.PedidoService;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Pedido;
import unpsjb.labprog.backend.model.EstadoPedido;

@RestController
@RequestMapping("orders")
public class PedidoPresenter {
    @Autowired
    PedidoService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> findAll() {
        return Response.ok(service.findAll());
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public ResponseEntity<Object> findByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EstadoPedido state) {
        return Response.ok(
                state != null ? service.findByPageAndState(page, size, state)
                        : service.findByPage(page, size));
    }

    @RequestMapping(value = "/planned", method = RequestMethod.GET)
    public ResponseEntity<Object> findAllPlanned() {
        return Response.ok(service.findByEstadoOrderByFechaEntregaAsc(EstadoPedido.PLANIFICADO));
    }

    @RequestMapping(value = "/search/{term}", method = RequestMethod.GET)
    public ResponseEntity<Object> search(
            @PathVariable String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EstadoPedido state) {
        return Response.ok(service.search(term, state, page, size));
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> findById(@PathVariable("id") int id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(value = "/cuit/{cuit}/deliveryDate/{deliveryDate}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByCustomerAndDeliveryDate(
            @PathVariable long cuit,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {
        return Response.ok(service.findByCustomerCuitAndDeliveryDate(cuit, deliveryDate));
    }

    @RequestMapping(value = "/id/{id}/plannings", method = RequestMethod.GET)
    public ResponseEntity<Object> findPlanningProcesses(@PathVariable("id") int id) {
        return Response.ok(service.findPlanningProcesses(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody Pedido aPedido) {
        if (aPedido.getId() != 0) {
            return Response.error("El id del pedido debe ser 0 o no estar presente.");
        }
        return Response.ok(service.save(aPedido),
                "Pedido de fabricación generado correctamente");
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Object> update(@RequestBody Pedido aPedido) {
        return Response.ok(service.save(aPedido));
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        try {
            service.delete(id);
            return Response.ok("Pedido id " + id + " eliminado con éxito.");
        } catch (BusinessException e) {
            return Response.conflict(e.getMessage());
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}
