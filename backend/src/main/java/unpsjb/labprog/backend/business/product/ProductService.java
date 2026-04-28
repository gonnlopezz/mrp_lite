package unpsjb.labprog.backend.business.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unpsjb.labprog.backend.business.equipment.EquipmentTypeRepository;
import unpsjb.labprog.backend.model.Product;

@Service
public class ProductService {
    @Autowired
    ProductRepository repository;

    @Autowired
    EquipmentTypeRepository equipmentTypeRepository;

    public List<Product> findAll() {
        List<Product> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Product> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Product> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }      

    public Product findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Optional<Product> findByName(String name) {
        return repository.findByName(name);

    }

    @Transactional
    public Product save(Product product) {
        return repository.save(product);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }
}
