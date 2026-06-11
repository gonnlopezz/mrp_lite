package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.equipo.TipoEquipoService;
import unpsjb.labprog.backend.model.TipoEquipo;

@RestController
@RequestMapping("/equipment-types")
public class TipoEquipoPresenter {
    @Autowired
    private TipoEquipoService service;

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

    @RequestMapping(value = "/search/{name}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByName(@PathVariable("name") String name) {       
        return Response.ok(service.findByName(name));
    }            

    @RequestMapping(value = "/id/{id}")
    public ResponseEntity<Object> findById(@PathVariable("id") int id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody TipoEquipo aTipoEquipo) {
        if (aTipoEquipo.getId() != 0) {
            return Response.error("El id del tipo de equipo debe ser 0 o no estar presente.");
        }
        return Response.ok(service.save(aTipoEquipo),
                "Tipo de equipo " + aTipoEquipo.getNombre() + " registrado correctamente");
    }


    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Object> update(@RequestBody TipoEquipo aTipoEquipo) {
        return Response.ok(service.save(aTipoEquipo));
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        service.delete(id);
        return Response.ok("Tipo de equipo id " + id + " eliminado con éxito.");
    }
}
