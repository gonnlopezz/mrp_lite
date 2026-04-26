package unpsjb.labprog.backend.business.equipment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import unpsjb.labprog.backend.model.Equipment;
import unpsjb.labprog.backend.model.EquipmentType;

@Service
public class EquipmentService {
    @Autowired
    EquipmentRepository repository;

    @Autowired
    EquipmentTypeRepository typeRepository;

    public void prepareForSaving(Equipment e) {
        if (e.getType() != null && e.getType().getName() != null) {
            EquipmentType realType = typeRepository.findByCode(e.getType().getName())
                .orElseThrow(() -> new RuntimeException("Tipo no encontrado"));
            e.setType(realType);
        }
    }
}

