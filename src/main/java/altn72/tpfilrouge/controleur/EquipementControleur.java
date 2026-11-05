package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.IncidentService;
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

    private final static int seuilIncident = 3;
    private final static String intervalleMaxIncident = "30 Day";


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
        if(incidentService.getNbIncidentIntervalle(seuilIncident, intervalleMaxIncident)){
            model.addAttribute("fragile", true);
        }
        else{
            model.addAttribute("fragile", false);
        }
        model.addAttribute("incidents", incidentService.getIncidents().stream().filter(incident -> incident.getEquipement().getId().equals(equipement.getId())).collect(Collectors.toList()));
        return "detailsEquipement";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @GetMapping("/nouveau")
    public String preparerAjoutEquipement(Model model) {
        model.addAttribute("nouveauEquipement", new Equipement());
        return "nouveauEquipement";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @DeleteMapping("/{id}")
    public String deleteEquipement(@PathVariable("id") Long id) {
        if (!equipementService.supprimerEquipement(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'equipement dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/equipements";
    }


    @ReserveAuProfil(Profil.RESPONSABLE)
    @PostMapping
    public String creerEquipement(@ModelAttribute Equipement equipement){
        equipementService.ajouterEquipement(equipement);
        return "redirect:/equipements";
    }


    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/{id}")
    public String modifierEquipement(@PathVariable("id") Long id,@ModelAttribute Equipement equipementModified ){
        if (equipementService.modifierEquipement(id,equipementModified).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'equipement dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/equipements";
    }

}
