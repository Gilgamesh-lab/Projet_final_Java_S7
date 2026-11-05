package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "L'équipement dont l'id est " + id + " n'existe pas"));
        model.addAttribute("equipement", equipement);
        if(incidentService.getNbIncidentIntervalle(seuilIncident, intervalleMaxIncident)){
            model.addAttribute("fragile", true);
        }
        else{
            model.addAttribute("fragile", false);
        }
        model.addAttribute("incidents", incidentService.getIncidentsEquipement(equipement.getId()));
        return "detailsEquipement";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @DeleteMapping("/{id}")
    public String deleteEquipement(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            if (!equipementService.supprimerEquipement(id)) {
                throw new RessourceIntrouvableException("L'équipement dont l'id est " + id + " n'existe pas");
            }
        } catch (RegleMetierException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/equipements";
    }

}
