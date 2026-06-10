package unpsjb.labprog.backend.business.equipo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.TipoEquipo;

@Repository
public interface TipoEquipoRepository extends CrudRepository<TipoEquipo, Integer>, PagingAndSortingRepository<TipoEquipo, Integer> {
    @Query("Select e FROM TipoEquipo e Where e.nombre = ?1")
    Optional<TipoEquipo> findByName(String name);
}
