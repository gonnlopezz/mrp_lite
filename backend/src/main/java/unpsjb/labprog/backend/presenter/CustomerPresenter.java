package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import unpsjb.labprog.backend.Response;
import unpsjb.labprog.backend.business.CustomerService;
import unpsjb.labprog.backend.model.Customer;

@RestController
@RequestMapping("customers")
public class CustomerPresenter {    
    @Autowired
    CustomerService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> findAll() {
        return Response.ok(service.findAll());
    }


    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> findById(@PathVariable("id") int id) {
        return Response.ok(service.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> create(@RequestBody Customer aCustomer) {
        if(aCustomer.getId() != 0) {
            return Response.error("El id del cliente debe ser 0 o no estar presente.");
        }
        return Response.ok(service.save(aCustomer), "Cliente " + aCustomer.getRazonSocial() + " (" + aCustomer.getCuit() + ") registrado correctamente");
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Object> update(@RequestBody Customer aCustomer) {
        return Response.ok(service.save(aCustomer));
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        service.delete(id);
        return Response.ok("Cliente id " + id + " eliminado con éxito.");
    }

}
