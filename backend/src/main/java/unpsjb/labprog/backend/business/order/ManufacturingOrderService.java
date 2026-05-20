package unpsjb.labprog.backend.business.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.model.ManufacturingOrder;

@Service
public class ManufacturingOrderService {
    @Autowired
    ManufacturingOrderRepository repository;

    public List<ManufacturingOrder> findAll() {
        List<ManufacturingOrder> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<ManufacturingOrder> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<ManufacturingOrder> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public ManufacturingOrder findById(long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public ManufacturingOrder save(ManufacturingOrder order) {
        return repository.save(order);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

}
