package unpsjb.labprog.backend.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import unpsjb.labprog.backend.business.planning.PlanningProcessService;
import unpsjb.labprog.backend.model.PlanningProcess;

@RestController
@RequestMapping("plannings")
public class PlanningPresenter {
    @Autowired
    PlanningProcessService service;

}
