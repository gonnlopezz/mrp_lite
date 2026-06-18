package unpsjb.labprog.backend.business.cliente;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import unpsjb.labprog.backend.business.pedido.PedidoRepository;
import unpsjb.labprog.backend.exception.BusinessException;
import unpsjb.labprog.backend.model.Cliente;

@Service
public class ClienteService {
    @Autowired
    ClienteRepository repository;

    @Autowired
    PedidoRepository pedidoRepository;

    public List<Cliente> findAll() {
        List<Cliente> result = new ArrayList<>();
        repository.findAll().forEach(e -> result.add(e));
        return result;
    }

    public Page<Cliente> findByPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Page<Cliente> search(String term, int page, int size) {
        return repository.search(term, PageRequest.of(page, size));
    }

    public Cliente findById(int id) {
        return repository.findById(id).orElse(null);
    }

    public Cliente findByCuit(String cuit) {
        return repository.findByCuit(cuit);
    }
    

    @Transactional
    public Cliente save(Cliente e) {
        return repository.save(e);
    }

    @Transactional
    public void delete(int id) {
        if (pedidoRepository.existsByClienteId(id)) {
            throw new BusinessException("No se puede eliminar el cliente porque tiene pedidos asociados.");
        }
        repository.deleteById(id);
    }
}
