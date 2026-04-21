package unpsjb.labprog.backend.business.equipment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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


    public EquipmentType findById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public EquipmentType save(EquipmentType e) {
        return repository.save(e);
    }

}
