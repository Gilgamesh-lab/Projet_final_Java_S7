package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.service.IncidentService;
import altn72.tpfilrouge.service.StationService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Controller
@RequestMapping("/incidents")
public class IncidentControleur {


    @Autowired
    private IncidentService incidentService;


    @GetMapping
    public String afficherInfosIncidents(Model model){
        List<Incident> liste = incidentService.getIncidents();
        model.addAttribute("incidents", liste);
        return "listeIncidents";
    }

    @GetMapping("/{id}")
    public String afficherUnIncident(@PathVariable("id") Long id, Model model) {
        Incident incident = incidentService.getUnIncident(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'incident dont l'id est " + id + " n'existe pas"));
        model.addAttribute("incidentToUpdate", incident);
        return "detailsIncident";
    }

    @GetMapping("/nouvelle")
    public String preparerAjoutIncident(Model model) {
        model.addAttribute("nouveauIncident", new Incident());
        return "nouveauIncident";
    }

    @DeleteMapping("/{id}")
    public String deleteIncident(@PathVariable("id") Long id) {
        if (!incidentService.supprimerIncident(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'incident dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/incidents";    // "redirect:" + une ROUTE (pas un nom de template) : ici celle de la liste
    }


    @PostMapping
    public String creerIncident(@ModelAttribute Incident incident){
        incidentService.ajouterIncident(incident);
        return "redirect:/incidents";
    }


    @PutMapping("/{id}")
    public String modifierIncident(@PathVariable("id") Long id,@ModelAttribute Incident incidentModified ){
        if (incidentService.modifierIncident(id,incidentModified).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'incident dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/incidents";
    }

}
