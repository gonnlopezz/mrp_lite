package unpsjb.labprog.backend.business.taller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.TipoEquipo;
import unpsjb.labprog.backend.model.Taller;

@Repository
public interface TallerRepository
                extends CrudRepository<Taller, Integer>, PagingAndSortingRepository<Taller, Integer> {
        @Query("Select e FROM Taller e Where e.codigo = ?1")
        Optional<Taller> findByCode(String code);

        @Query("SELECT DISTINCT t FROM Taller t LEFT JOIN FETCH t.equipos ORDER BY t.id ASC")
        List<Taller> findAllConEquipos();

        @Query("SELECT w FROM Taller w " +
                        "WHERE w.codigo ILIKE CONCAT('%', :term, '%') " +
                        "OR w.nombre ILIKE CONCAT('%', :term, '%')")
        Page<Taller> search(String term, Pageable pageable);

        @Query("SELECT DISTINCT t FROM Taller t " +
                        "LEFT JOIN FETCH t.equipos " +
                        "WHERE t.id IN (" +
                        "  SELECT w.id FROM Taller w JOIN w.equipos e WHERE e.tipo IN :types GROUP BY w.id HAVING COUNT(DISTINCT e.tipo) = :count"
                        +
                        ") ORDER BY t.id ASC")
        List<Taller> findPosiblesTalleresConEquipos(@Param("types") List<TipoEquipo> types,
                        @Param("count") int count);

}
