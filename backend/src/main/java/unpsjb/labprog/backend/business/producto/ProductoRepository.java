package unpsjb.labprog.backend.business.producto;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import unpsjb.labprog.backend.model.Producto;

@Repository
public interface ProductoRepository extends CrudRepository<Producto, Integer>, PagingAndSortingRepository<Producto, Integer> {
    @Query("Select e FROM Producto e Where e.nombre = ?1")
    Optional<Producto> findByName(String name);

    @Query("SELECT p FROM Producto p" + 
    " WHERE p.nombre ILIKE CONCAT('%', :term, '%')")
    public Page<Producto> search(String term, Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Producto p JOIN p.tareas t WHERE t.tipo.id = :tipoEquipoId")
    boolean existsTareaWithTipo(@Param("tipoEquipoId") int tipoEquipoId);

}
