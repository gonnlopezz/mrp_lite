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

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<Tarea> tareas;

    // Métodos

    public List<TipoEquipo> tiposDeEquipoRequeridos() {
        List<TipoEquipo> resultado = new ArrayList<>();
        for (Tarea tarea : this.getTareas()) {
            TipoEquipo tipo = tarea.getTipo();
            if (tipo != null && !resultado.contains(tipo))
                resultado.add(tipo);
        }
        return resultado;
    }

    public List<Tarea> tareasEnOrdenInverso() {
        List<Tarea> resultado = new ArrayList<>(this.getTareas());
        Collections.reverse(resultado);
        return resultado;
    }

}
