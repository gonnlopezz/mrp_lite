package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.planning.PlanningProcessService;
import unpsjb.labprog.backend.dto.PlanningRequestDTO;
import unpsjb.labprog.backend.exception.BusinessException;

@RestController
@RequestMapping("plannings")
public class PlanningPresenter {
    @Autowired
    PlanningProcessService service;

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

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> createPlanning(@RequestBody PlanningRequestDTO request) {
        if (request.getWorkshopCode() != null && request.getWorkshopCode().isBlank()) {
            request.setWorkshopCode(null);
        }
        try {
            return Response.ok(service.save(request), "Producto planificado con éxito");
        } catch (BusinessException e) {
            return Response.conflict(e.getMessage()); // 409
        } 
         catch (EntityNotFoundException e) {
            return Response.notFound(e.getMessage()); // 404
        } catch (Exception e) {
            return Response.error(e.getMessage()); // 400 para el resto
        }
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") long id) {
        service.delete(id);
        return Response.ok("Planificación id " + id + " eliminada con éxito.");
    }
}
