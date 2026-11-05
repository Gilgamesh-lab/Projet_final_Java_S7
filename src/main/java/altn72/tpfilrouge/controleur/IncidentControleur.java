package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/incidents")
@Tag(name = "Incidents", description = "Gestion des incidents")
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


    @Operation(summary = "Récupérer la liste de incidents", description = "Renvoie la liste des incidents")
    @GetMapping("/api")
    public ResponseEntity afficherInfosIncidents(){
        List<Incident> liste = incidentService.getIncidents();
        Map<String, Object> response = new HashMap<>();
        response.put("incidents", liste);
        return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
    }

    @Operation(summary = "Modififier un incident", description = "Modifie un incident grâce à son id en l'écrasant par le nouveau incident fournie")
    @PutMapping("api/modifier/{id}")
    public ResponseEntity modifierIncident(@RequestBody Incident incident, @PathVariable("id") Long id){
        if(incidentService.getUnIncident(id).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type", "application/json").body(null);
        }
        else{
            incidentService.modifierIncident(id,incident);
            Map<String, Object> response = new HashMap<>();
            incident.getEvaluation().setIncident(null);
            response.put("incidentModifié", incident);
            return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
        }

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
        if(Duration.between(incident.getDateOuverture(), incident.getDateResolution()).toMinutes() >= 60) {
            model.addAttribute("durée", Duration.between(incident.getDateOuverture(), incident.getDateResolution()).toHours() + "h");
        } else if (Duration.between(incident.getDateOuverture(), incident.getDateResolution()).toHours() >= 24) {
            model.addAttribute("durée", Duration.between(incident.getDateOuverture(), incident.getDateResolution()).toHours() + "j");
        }
        else{
            model.addAttribute("durée", Duration.between(incident.getDateOuverture(), incident.getDateResolution()).toMinutes() + "min");
        }

        return "detailsIncident";
    }



    @DeleteMapping("/{id}")
    public String deleteIncident(@PathVariable("id") Long id) {
        if (!incidentService.supprimerIncident(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "L'incident dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/equipements";
    }


    @GetMapping("/declarer")
    public String declarerUnIncident(Model model){
        model.addAttribute("nouveauIncident", new Incident());
        model.addAttribute("equipements", equipementService.getEquipements());
        model.addAttribute("statuts", Statut.values());
        model.addAttribute("erreur", "");
        return "nouveauIncident";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/changerPrestataire/{id}")
    public String changerPrestataire(@PathVariable("id") Long id, @RequestParam("prestataire") Long prestataire) {
        Incident incident = incidentService.getUnIncident(id).get();
        if(incident.getStatut() != Statut.RESOLU){
            incident.setPrestataire(prestataireService.getPrestataireById(prestataire));
            incident.setStatut(Statut.EN_COURS);
            incidentService.modifierIncident(id, incident);

        }
        return "redirect:/incidents/tableau-de-bord";
    }

    @GetMapping("/exporter")
    public ResponseEntity<byte[]> exporter()  {
        List<Incident> incidents = incidentService.getIncidents();
        incidents.stream().forEach(incident -> {incident.setEvaluation(null);}); // pansement pour éviter de devoir créer une classe DTO
        ObjectMapper objectMapper =  new ObjectMapper();
        byte[] json = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(incidents);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("incidents.json").build().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/resolu/{id}")
    public String incidentResolu(@PathVariable("id") Long id) {
        Incident incident = incidentService.getUnIncident(id).get();

        if(incident.getStatut() != Statut.RESOLU){
            incident.setStatut(Statut.RESOLU);
            incident.setDateResolution(LocalDateTime.now());
            incidentService.modifierIncident(id, incident);
        }
        
        return "redirect:/incidents/tableau-de-bord";
    }

    @PostMapping("/nouveau")
    public String creerIncident(@ModelAttribute("nouveauIncident") Incident incident, Model model) {
        if(incidentService.getIncidentByIdentifiant(incident.getIdentifiant()) != null){
            model.addAttribute("erreur", "Cet identifiant existe déjà, veuillez en choisir un autre");
            model.addAttribute("nouveauIncident", new Incident());
            model.addAttribute("equipements", equipementService.getEquipements());
            model.addAttribute("statuts", Statut.values());
            return "nouveauIncident";
        }
        else{
            incident.setDateOuverture(LocalDateTime.now());
            incident.setStatut(Statut.NOUVEAU);
            incidentService.ajouterIncident(incident);
            return "redirect:/";
        }

    }

    @PutMapping("/modifier/{id}")
    public String modifierIncident(@PathVariable("id") Long id,@ModelAttribute("incidentToUpdate") Incident incidentModified ){
        Incident incident = incidentService.getUnIncident(id).get();
        incident.setDescription(incidentModified.getDescription());
        incidentService.modifierIncident(id,incident);
        return "redirect:/equipements";
    }

}
