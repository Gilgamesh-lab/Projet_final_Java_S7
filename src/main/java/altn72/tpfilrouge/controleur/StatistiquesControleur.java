package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.StatistiquesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ReserveAuProfil(Profil.RESPONSABLE)
public class StatistiquesControleur {

    private final StatistiquesService statistiquesService;

    public StatistiquesControleur(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping("/statistiques")
    public String afficherStatistiques(Model model) {
        model.addAttribute("parStation", statistiquesService.getActifsParStation());
        model.addAttribute("parIndicateur", statistiquesService.getRepartitionParIndicateur());
        model.addAttribute("notesPrestataires", statistiquesService.getNoteMoyenneParPrestataire());
        return "statistiques";
    }
}
