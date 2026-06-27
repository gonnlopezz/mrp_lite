package unpsjb.labprog.backend.business.planificacion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import unpsjb.labprog.backend.model.Taller;

@Component
public class OrdenadorTaller {

    public List<Taller> ordenarPorDisponibilidad(List<Taller> talleres,
            Map<Long, Agenda> agendas, LocalDateTime deadline) {

        List<Taller> resultado = new ArrayList<>(talleres);

        resultado.sort((a, b) -> Long.compare(
                agendas.get(b.getId()).calcularTiempoLibreHasta(deadline),
                agendas.get(a.getId()).calcularTiempoLibreHasta(deadline)));

        return resultado;
    }
}
