package unpsjb.labprog.backend.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Equipo encontrarEquipamientoPara(TipoEquipo tipo) {
        return equipos.stream().filter(e -> e.getTipo().equals(tipo)).findFirst()
                .orElseThrow(() -> new BusinessException(
                        "El taller no cuenta con el tipo de equipo requerido: " + tipo.getNombre()));
    }

    public boolean soportaEquipamiento(List<TipoEquipo> tiposRequeridos) {
        Set<TipoEquipo> tiposDisponibles = new HashSet<>();
        for (Equipo equipo : this.equipos) {
            tiposDisponibles.add(equipo.getTipo());
        }
        return tiposDisponibles.containsAll(tiposRequeridos);
    }
}
