package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.workshop.WorkshopService;
import unpsjb.labprog.backend.model.Workshop;

@RestController
@RequestMapping("/workshops")
public class WorkshopPresenter {
    @Autowired
    WorkshopService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> findAll() {
        return Response.ok(service.findAll());      
    }
    
    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> findById(@PathVariable("id") int id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(value = "/code/{code}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByCode(@PathVariable("code") String code) {
        return Response.ok(service.findByCode(code));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody Workshop aWorkshop) {
        return Response.ok(service.save(aWorkshop), "Taller " + aWorkshop.getCode() + " ingresado correctamente");
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Object> update(@RequestBody Workshop aWorkshop) {
        return Response.ok(service.save(aWorkshop), "Taller " + aWorkshop.getCode() + " actualizado correctamente");
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        service.delete(id);
        return Response.ok("Taller id " + id + " eliminado con éxito.");
    }
}
