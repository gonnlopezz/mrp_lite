package unpsjb.labprog.backend.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE } , orphanRemoval = true)
    private Collection<Task> tasks;


    // Métodos

    public List<EquipmentType> requiredEquipmentTypes() {
        List<EquipmentType> result = new ArrayList<>();
        for (Task task : this.getTasks()) {
            EquipmentType type = task.getType();
            if (type != null && !result.contains(type))
                result.add(type);
        }
        return result;
    }

}
