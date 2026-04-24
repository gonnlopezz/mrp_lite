package unpsjb.labprog.backend.business.workshop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.model.Workshop;

@Service
public class WorkshopService {
    @Autowired
    WorkshopRepository repository;

    public List<Workshop> findAll() {
        List<Workshop> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Workshop findById(long id) {
        return repository.findById(id).orElse(null);
    }

    public Workshop findByCode(String code) {
        return repository.findByCode(code).orElse(null);
    }

    @Transactional
    public Workshop save(Workshop e) {
        return repository.save(e);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id); 
    }

}
