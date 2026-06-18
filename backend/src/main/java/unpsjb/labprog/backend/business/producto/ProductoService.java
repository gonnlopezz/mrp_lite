package unpsjb.labprog.backend.business.producto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import unpsjb.labprog.backend.business.pedido.PedidoRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Producto;

@Service
public class ProductoService {
    @Autowired
    ProductoRepository repository;

    @Autowired
    PedidoRepository pedidoRepository;

    public List<Producto> findAll() {
        List<Producto> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Producto> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Producto> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public Page<Producto> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }      

    public Producto findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Producto findByName(String name) {
        return repository.findByName(name).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

    }

    @Transactional
    public Producto save(Producto product) {
        return repository.save(product);
    }

    @Transactional
    public void delete(int id) {
        if (pedidoRepository.existsByProductoId(id)) {
            throw new BusinessException("No se puede eliminar el producto porque tiene pedidos asociados.");
        }
        repository.deleteById(id);
    }
}
