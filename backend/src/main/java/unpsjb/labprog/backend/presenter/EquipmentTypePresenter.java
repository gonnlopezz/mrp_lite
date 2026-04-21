package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value = "/id/{id}")
    public ResponseEntity<Object> findById(@PathVariable ("id") int id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody EquipmentType aEquipmentType) {
        if(aEquipmentType.getId() != 0) {
            return Response.error("El id del tipo de equipo debe ser 0 o no estar presente.");
        }
        return Response.ok(service.save(aEquipmentType), "Tipo de equipo " + aEquipmentType.getNombre() + " registrado correctamente");
    }
}
