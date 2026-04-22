package unpsjb.labprog.backend.business.equipment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.model.EquipmentType;

@Service
public class EquipmentTypeService {
    @Autowired
    EquipmentTypeRepository repository;

    public List<EquipmentType> findAll() {
        List<EquipmentType> result = new ArrayList<>();
        repository.findAll().forEach(e-> result.add(e));
        return result;
    }

    public Page<EquipmentType> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }



    public EquipmentType findById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public EquipmentType save(EquipmentType e) {
        return repository.save(e);
    }

    @Transactional
    public void delete(int id) {
        repository.deleteById(id);
    }

}
