package unpsjb.labprog.backend.model;

import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unpsjb.labprog.backend.exception.BusinessException;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Workshop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private Collection<Equipment> equipments;

    // Métodos

    public Equipment findEquipmentForType(EquipmentType type) {
        for(Equipment eq : equipments) {
            if (eq.getType().equals(type))
                return eq;
        }
        throw new BusinessException("El taller no cuenta con el tipo de equipo requerido: " + type.getName());
    }
}
