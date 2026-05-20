package unpsjb.labprog.backend.business.customer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.model.Customer;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository repository;

    public List<Customer> findAll() {
        List<Customer> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Customer> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Customer> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public Customer findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Customer findByCuit(String cuit) {
        return repository.findByCuit(cuit);
    }
    

    @Transactional
    public Customer save(Customer e) {
        return repository.save(e);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }
}
