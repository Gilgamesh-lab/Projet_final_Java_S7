package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/incidents")
public class IncidentControleur {


    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IndicateurService indicateurService;

    @Autowired
    private TableauDeBordService tableauDeBordService;
    @Autowired
    private StationService stationService;


    @GetMapping
    public String afficherInfosIncidents(Model model){
        List<Incident> liste = incidentService.getIncidents();
        model.addAttribute("incidents", liste);
        return "listeIncidents";
    }

    @GetMapping("/historique")
    public String afficherHistorique(Model model){
        List<Incident> liste = incidentService.getIncidentsResolue();
        model.addAttribute("incidents", liste);
        return "historiqueIncidents";
    }


    @ReserveAuProfil(Profil.RESPONSABLE)
    @GetMapping("/tableau-de-bord")
    public String afficherTableauDeBordFilter(@RequestParam(required = false, value = "station") String station, @RequestParam(required = false, value = "niveau") String niveau, Model model){
        List<Incident> liste;
        if((station == null || station.isEmpty()) && (niveau == null ||niveau.isEmpty()) ){
            liste = incidentService.getIncidentsEnCours();
        }

        } else{
            Indicateur indicateur = null;
            if(niveau != null && !niveau.isEmpty()){
                indicateur = Indicateur.valueOf(niveau.toUpperCase().replace(" ", "_"));
            }

            Long stationId = null;
            if(station != null && !station.isEmpty()){
                stationId = stationService.getUneStation(station).getId();
            }

            liste = tableauDeBordService.getLignes(stationId, indicateur).stream().map(LigneTableauDeBord::getIncident).collect(Collectors.toList());
        }

        model.addAttribute("incidents", liste);
        model.addAttribute("listeNiveau", Indicateur.values());
        model.addAttribute("listeStation", stationService.getStations().stream().map(Station::getNom).collect(Collectors.toList()));
        model.addAttribute("compteurs", tableauDeBordService.getCompteurs());
        model.addAttribute("actifsParStation", tableauDeBordService.getActifsParStation());
        model.addAttribute("nbIncident", liste.size());
        model.addAttribute("nbIncidentRouge", liste.stream().filter(incident -> indicateurService.calculer(incident).equals(Indicateur.ROUGE)).count());
        model.addAttribute("nbIncidentOrange", liste.stream().filter(incident -> indicateurService.calculer(incident).equals(Indicateur.ORANGE)).count());
        model.addAttribute("nbIncidentPrisEnCharge", liste.stream().filter(incident -> indicateurService.calculer(incident).equals(Indicateur.PRIS_EN_CHARGE)).count());
        model.addAttribute("niveau", liste.stream().map(incident -> indicateurService.calculer(incident)).collect(Collectors.toList()));
        return "tableauDeBord";
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
        return "redirect:/listeEquipement";    // "redirect:" + une ROUTE (pas un nom de template) : ici celle de la liste
    }


    @PostMapping
    public String creerIncident(@ModelAttribute Incident incident){
        incidentService.ajouterIncident(incident);
        return "redirect:/equipements";
    }


    @PutMapping("/modifier/{id}")
    public String modifierIncident(@PathVariable("id") Long id,@ModelAttribute Incident incidentModified ){
        incidentService.modifierIncident(id,incidentModified);
        return "redirect:/equipements";
    }

}
