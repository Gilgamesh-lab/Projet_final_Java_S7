package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.IncidentService;
import altn72.tpfilrouge.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/equipements")
public class EquipementControleur {


    @Autowired
    private EquipementService equipementService;

    @Autowired
    private IncidentService incidentService;


    @GetMapping
    public String afficherInfosEquipements(@RequestParam(required = false, value = "station") String station, Model model){
        List<Equipement> liste;
        if(station == null || station.isEmpty()){
            liste = equipementService.getEquipements();
        }
        else{
            liste = equipementService.getEquipements().stream().filter(equipement -> equipement.getStation().getNom().equals(station)).collect(Collectors.toList());
        }
        model.addAttribute("equipements", liste);
        return "listeEquipements";
    }

    @GetMapping("/{id}")
    public String afficherUnEquipement(@PathVariable("id") Long id, Model model) {
        Equipement equipement = equipementService.getUnEquipement(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'equipement dont l'id est " + id + " n'existe pas"));
        model.addAttribute("equipement", equipement);
        model.addAttribute("incidents", incidentService.getIncidents().stream().filter(incident -> incident.getEquipement().getId().equals(equipement.getId())).collect(Collectors.toList()));
        return "detailsEquipement";
    }

    @GetMapping("/nouveau")
    public String preparerAjoutEquipement(Model model) {
        model.addAttribute("nouveauEquipement", new Equipement());
        return "nouveauEquipement";
    }

    @DeleteMapping("/{id}")
    public String deleteEquipement(@PathVariable("id") Long id) {
        if (!equipementService.supprimerEquipement(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'equipement dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/equipements";    // "redirect:" + une ROUTE (pas un nom de template) : ici celle de la liste
    }


    @PostMapping
    public String creerEquipement(@ModelAttribute Equipement equipement){
        equipementService.ajouterEquipement(equipement);
        return "redirect:/equipements";
    }


    @PutMapping("/{id}")
    public String modifierEquipement(@PathVariable("id") Long id,@ModelAttribute Equipement equipementModified ){
        if (equipementService.modifierEquipement(id,equipementModified).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'equipement dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/equipements";
    }

}
