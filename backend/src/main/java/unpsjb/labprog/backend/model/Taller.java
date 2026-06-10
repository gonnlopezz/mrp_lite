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
public class Taller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private Collection<Equipo> equipos;

    // Métodos

    public Equipo findEquipmentForType(TipoEquipo type) {
        for(Equipo eq : equipos) {
            if (eq.getTipo().equals(type))
                return eq;
        }
        throw new BusinessException("El taller no cuenta con el tipo de equipo requerido: " + type.getNombre());
    }
}
