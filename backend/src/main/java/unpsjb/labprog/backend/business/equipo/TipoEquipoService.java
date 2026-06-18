package unpsjb.labprog.backend.business.equipo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.taller.TallerRepository;
import unpsjb.labprog.backend.business.producto.ProductoRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.TipoEquipo;

@Service
public class TipoEquipoService {
    @Autowired
    TipoEquipoRepository repository;

    @Autowired
    TallerRepository tallerRepository;

    @Autowired
    ProductoRepository productoRepository;

    public List<TipoEquipo> findAll() {
        List<TipoEquipo> result = new ArrayList<>();
        repository.findAll().forEach(e-> result.add(e));
        return result;
    }

    public Page<TipoEquipo> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public TipoEquipo findByName(String name) {
        return repository.findByName(name).orElse(null);
    }


    public TipoEquipo findById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public TipoEquipo save(TipoEquipo e) {
        return repository.save(e);
    }

    @Transactional
    public void delete(int id) {
        if (tallerRepository.existsEquipoWithTipo(id)) {
            throw new BusinessException("No se puede eliminar el tipo de equipo porque está asignado a un equipo en un taller.");
        }
        if (productoRepository.existsTareaWithTipo(id)) {
            throw new BusinessException("No se puede eliminar el tipo de equipo porque está asignado a una tarea de un producto.");
        }
        repository.deleteById(id);
    }

}
