package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Autowired
    private FragiliteService fragiliteService;


    @Operation(summary = "Récupérer la liste des incidents", description = "Renvoie la liste des incidents")
    @GetMapping("/api")
    public ResponseEntity afficherInfosIncidents(){
        List<Incident> liste = incidentService.getIncidents();
        Map<String, Object> response = new HashMap<>();
        response.put("incidents", liste);
        return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @Operation(summary = "Modifier un incident", description = "Modifie la description d'un incident non résolu ou le passe au statut RESOLU. Répond 400 si la saisie est invalide, 404 si l'incident n'existe pas et 409 s'il est déjà résolu")
    @PutMapping("api/modifier/{id}")
    public ResponseEntity modifierIncident(@RequestBody Incident incident, @PathVariable("id") Long id){
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("incidentModifié", incidentService.modifierDepuisApi(id, incident.getDescription(), incident.getStatut()));
            return ResponseEntity.status(HttpStatus.OK).header("Content-Type", "application/json").body(response);
        } catch (RessourceIntrouvableException e) {
            response.put("erreur", e.getReason());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type", "application/json").body(response);
        } catch (RegleMetierException e) {
            response.put("erreur", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).header("Content-Type", "application/json").body(response);
        } catch (SaisieInvalideException e) {
            response.put("erreur", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(response);
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
        Indicateur indicateur = Arrays.stream(Indicateur.values())
                .filter(choix -> choix.name().equalsIgnoreCase(niveau)).findFirst().orElse(null);
        Long stationId = null;
        if(station != null && !station.isEmpty()){
            stationId = stationService.getUneStation(station).getId();
        }
        List<Incident> liste = tableauDeBordService.getLignes(stationId, indicateur).stream().map(LigneTableauDeBord::getIncident).collect(Collectors.toList());

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
        model.addAttribute("equipementsFragiles", fragiliteService.getEquipementsFragiles());
        model.addAttribute("niveau", liste.stream().map(incident -> indicateurService.calculer(incident)).collect(Collectors.toList()));
        return "tableauDeBord";
    }

    @GetMapping("/{id}")
    public String afficherUnIncident(@PathVariable("id") Long id, Model model) {
        Incident incident = incidentService.getUnIncident(id)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "L'incident dont l'id est " + id + " n'existe pas"));
        model.addAttribute("incidentToUpdate", incident);
        LocalDateTime fin = incident.getDateResolution() != null ? incident.getDateResolution() : LocalDateTime.now();
        Duration duree = Duration.between(incident.getDateOuverture(), fin);
        if(duree.toHours() >= 24) {
            model.addAttribute("durée", duree.toDays() + "j");
        } else if (duree.toMinutes() >= 60) {
            model.addAttribute("durée", duree.toHours() + "h");
        }
        else{
            model.addAttribute("durée", duree.toMinutes() + "min");
        }

        return "detailsIncident";
    }



    @ReserveAuProfil(Profil.RESPONSABLE)
    @DeleteMapping("/{id}")
    public String deleteIncident(@PathVariable("id") Long id) {
        if (!incidentService.supprimerIncident(id)) {
            throw new RessourceIntrouvableException(
                    "L'incident dont l'id est " + id + " n'existe pas");
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
    public String changerPrestataire(@PathVariable("id") Long id, @RequestParam("prestataire") Long prestataire, RedirectAttributes redirect) {
        try {
            incidentService.affecterPrestataire(id, prestataire);
        } catch (RegleMetierException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/incidents/tableau-de-bord";
    }

    @GetMapping("/exporter")
    public ResponseEntity<byte[]> exporter()  {
        List<Incident> incidents = incidentService.getIncidentsResolue();
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
    public String incidentResolu(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            incidentService.declarerResolu(id);
        } catch (RegleMetierException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/incidents/tableau-de-bord";
    }

    @PostMapping("/nouveau")
    public String creerIncident(@ModelAttribute("nouveauIncident") Incident incident, Model model, RedirectAttributes redirect) {
        try {
            incidentService.declarerIncident(incident);
            String message = "L'incident " + incident.getIdentifiant() + " a bien été déclaré.";
            if(fragiliteService.estFragile(incident.getEquipement().getId())){
                message += " Attention : cet équipement est fragile.";
            }
            redirect.addFlashAttribute("message", message);
            return "redirect:/";
        } catch (SaisieInvalideException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("equipements", equipementService.getEquipements());
            model.addAttribute("statuts", Statut.values());
            return "nouveauIncident";
        }
    }

    @PutMapping("/modifier/{id}")
    public String modifierIncident(@PathVariable("id") Long id,@ModelAttribute("incidentToUpdate") Incident incidentModified, RedirectAttributes redirect){
        try {
            incidentService.modifierDescription(id, incidentModified.getDescription());
            return "redirect:/equipements";
        } catch (RegleMetierException | SaisieInvalideException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/incidents/" + id;
        }
    }

}
