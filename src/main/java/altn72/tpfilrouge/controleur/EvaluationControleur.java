package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.EvaluationService;
import altn72.tpfilrouge.service.IncidentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/incidents/{id}/evaluation")
@ReserveAuProfil(Profil.RESPONSABLE)
public class EvaluationControleur {

    private final EvaluationService evaluationService;
    private final IncidentService incidentService;

    public EvaluationControleur(EvaluationService evaluationService, IncidentService incidentService) {
        this.evaluationService = evaluationService;
        this.incidentService = incidentService;
    }

    @GetMapping
    public String afficherFormulaire(@PathVariable("id") Long id, Model model) {
        Incident incident = trouverIncident(id);
        model.addAttribute("incident", incident);
        try {
            evaluationService.verifierEvaluable(incident);
            model.addAttribute("evaluable", true);
        } catch (RegleMetierException e) {
            model.addAttribute("evaluable", false);
            model.addAttribute("erreur", e.getMessage());
        }
        return "nouvelleEvaluation";
    }

    @PostMapping
    public String enregistrer(@PathVariable("id") Long id,
                              @RequestParam(name = "note", required = false) Integer note,
                              @RequestParam(name = "commentaire", defaultValue = "") String commentaire,
                              Model model) {
        Incident incident = trouverIncident(id);
        try {
            evaluationService.evaluer(incident, note, commentaire);
            return "redirect:/incidents/historique";
        } catch (RegleMetierException | SaisieInvalideException e) {
            model.addAttribute("incident", incident);
            model.addAttribute("evaluable", e instanceof SaisieInvalideException);
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("note", note);
            model.addAttribute("commentaire", commentaire);
            return "nouvelleEvaluation";
        }
    }

    private Incident trouverIncident(Long id) {
        return incidentService.getUnIncident(id)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "L'incident dont l'id est " + id + " n'existe pas"));
    }
}
