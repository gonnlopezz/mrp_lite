package unpsjb.labprog.backend.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE } , orphanRemoval = true)
    private Collection<Tarea> tareas;


    // Métodos

    public List<TipoEquipo> requiredEquipmentTypes() {
        List<TipoEquipo> result = new ArrayList<>();
        for (Tarea task : this.getTareas()) {
            TipoEquipo type = task.getTipo();
            if (type != null && !result.contains(type))
                result.add(type);
        }
        return result;
    }

        public List<Tarea> getReverseTasks() {
        List<Tarea> result = new ArrayList<>(this.getTareas());
        Collections.reverse(result);
        return result;
    }

}
