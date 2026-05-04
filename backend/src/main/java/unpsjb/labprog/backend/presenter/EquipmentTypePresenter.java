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
import unpsjb.labprog.backend.business.equipment.EquipmentTypeService;
import unpsjb.labprog.backend.model.EquipmentType;

@RestController
@RequestMapping("/equipment-types")
public class EquipmentTypePresenter {
    @Autowired
    private EquipmentTypeService service;

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
    public ResponseEntity<Object> create(@RequestBody EquipmentType aEquipmentType) {
        if (aEquipmentType.getId() != 0) {
            return Response.error("El id del tipo de equipo debe ser 0 o no estar presente.");
        }
        return Response.ok(service.save(aEquipmentType),
                "Tipo de equipo " + aEquipmentType.getName() + " registrado correctamente");
    }


    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Object> update(@RequestBody EquipmentType aEquipmentType) {
        return Response.ok(service.save(aEquipmentType));
    }

    @RequestMapping(value = "/id/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        return Response.ok("Tipo de equipo id " + id + " eliminado con éxito.");
    }
}
