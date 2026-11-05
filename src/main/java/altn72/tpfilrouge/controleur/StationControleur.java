package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.securite.ReserveAuProfil;
import altn72.tpfilrouge.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/stations")
public class StationControleur {


    @Autowired
    private StationService stationService;


    @GetMapping
    public String afficherInfosStations(Model model){
        List<Station> liste = stationService.getStations();
        model.addAttribute("stations", liste);
        return "listeStations";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @GetMapping("/{id}")
    public String afficherUneStation(@PathVariable("id") Long id, Model model) {
        Station station = stationService.getUneStation(id)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "La station dont l'id est " + id + " n'existe pas"));
        model.addAttribute("stationToUpdate", station);
        return "detailsStation";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @GetMapping("/nouvelle")
    public String preparerAjoutStation(Model model) {
        model.addAttribute("nouvelleStation", new Station());
        return "nouvelleStation";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @DeleteMapping("/{id}")
    public String deleteStation(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            if (!stationService.supprimerStation(id)) {
                throw new RessourceIntrouvableException("La station dont l'id est " + id + " n'existe pas");
            }
        } catch (RegleMetierException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/stations";
    }


    @ReserveAuProfil(Profil.RESPONSABLE)
    @PostMapping("/creer")
    public String creerStation(@ModelAttribute("nouvelleStation") Station station, RedirectAttributes redirect){
        try {
            stationService.ajouterStation(station);
        } catch (SaisieInvalideException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erreur", "Cet identifiant de station existe déjà.");
        }
        return "redirect:/stations";
    }

    @ReserveAuProfil(Profil.RESPONSABLE)
    @PostMapping("/importer")
    public String importer(@RequestParam("fichier") MultipartFile fichier,
                           RedirectAttributes redirect) {
        if (fichier.isEmpty()) {
            redirect.addFlashAttribute("erreur", "Aucun fichier sélectionné");
            return "redirect:/stations";
        }
        try {
            int nb = stationService.importerJson(fichier);
            redirect.addFlashAttribute("message", nb + " station(s) importée(s)");
        } catch (IOException | JacksonException e) {
            redirect.addFlashAttribute("erreur", "Le fichier n'est pas une liste de stations au format JSON.");
        } catch (SaisieInvalideException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erreur", "Import annulé : un identifiant de station existe déjà.");
        }
        return "redirect:/stations";
    }



    @ReserveAuProfil(Profil.RESPONSABLE)
    @PutMapping("/{id}")
    public String modifierStation(@PathVariable("id") Long id,@ModelAttribute Station stationModified, RedirectAttributes redirect){
        try {
            if (stationService.modifierStation(id,stationModified).isEmpty()) {
                throw new RessourceIntrouvableException("La station dont l'id est " + id + " n'existe pas");
            }
        } catch (SaisieInvalideException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erreur", "Cet identifiant de station existe déjà.");
        }
        return "redirect:/stations";
    }

}
