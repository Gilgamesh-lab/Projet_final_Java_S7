package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/incidents")
public class IncidentControleur {


    @Autowired
    private IncidentService incidentService;

    @Autowired
    private EquipementService equipementService;

    @Autowired
    private IndicateurService indicateurService;

    @Autowired
    private TableauDeBordService tableauDeBordService;

    @Autowired
    private StationService stationService;

    @Autowired
    private PrestataireService prestataireService;


    /*@GetMapping
    public String afficherInfosIncidents(Model model){
        List<Incident> liste = incidentService.getIncidents();
        model.addAttribute("incidents", liste);
        return "pageAccueil";
    }*/

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

        else{
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
        model.addAttribute("listePrestataire", prestataireService.getPrestataires());
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



    @DeleteMapping("/{id}")
    public String deleteIncident(@PathVariable("id") Long id) {
        if (!incidentService.supprimerIncident(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'incident dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/listeEquipement";    // "redirect:" + une ROUTE (pas un nom de template) : ici celle de la liste
    }


    @GetMapping("/declarer")
    public String declarerUnIncident(Model model){
        model.addAttribute("nouveauIncident", new Incident());
        model.addAttribute("equipements", equipementService.getEquipements());
        model.addAttribute("statuts", Statut.values());
        return "nouveauIncident";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/changerPrestataire/{id}")
    public String changerPrestataire(@PathVariable Long id, @RequestParam Long prestataire) {
        Incident incident = incidentService.getUnIncident(id).get();
        incident.setPrestataire(prestataireService.getPrestataireById(prestataire));
        incidentService.modifierIncident(id, incident);
        return "redirect:/incidents/tableau-de-bord";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/resolu/{id}")
    public String incidentResolu(@PathVariable Long id) {
        Incident incident = incidentService.getUnIncident(id).get();
        incident.setStatut(Statut.RESOLU);
        incident.setDateResolution(LocalDateTime.now());
        incidentService.modifierIncident(id, incident);
        return "redirect:/incidents/tableau-de-bord";
    }

    @PostMapping("/nouveau")
    public String creerIncident(@ModelAttribute("nouveauIncident") Incident incident) {
        incident.setDateOuverture(LocalDateTime.now());
        incidentService.ajouterIncident(incident);
        return "redirect:/";
    }

    @PutMapping("/modifier/{id}")
    public String modifierIncident(@PathVariable("id") Long id,@ModelAttribute Incident incidentModified ){
        incidentService.modifierIncident(id,incidentModified);
        return "redirect:/equipements";
    }

}
