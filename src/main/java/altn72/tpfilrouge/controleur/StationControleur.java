package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


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

    @GetMapping("/{id}")
    public String afficherUneStation(@PathVariable("id") Long id, Model model) {
        Station station = stationService.getUneStation(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La station dont l'id est " + id + " n'existe pas"));
        model.addAttribute("stationToUpdate", station);
        return "detailsStation";
    }

    @GetMapping("/nouvelle")
    public String preparerAjoutStation(Model model) {
        model.addAttribute("nouvelleStation", new Station());
        return "nouvelleStation";
    }

    @DeleteMapping("/{id}")
    public String deleteStation(@PathVariable("id") Long id) {
        if (!stationService.supprimerStation(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "La station dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/stations";    // "redirect:" + une ROUTE (pas un nom de template) : ici celle de la liste
    }


    @PostMapping
    public String creerStation(@ModelAttribute Station station){
        stationService.ajouterStation(station);
        return "redirect:/stations";
    }


    @PutMapping("/{id}")
    public String modifierStation(@PathVariable("id") Long id,@ModelAttribute Station stationModified ){
        if (stationService.modifierstation(id,stationModified).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Le Station dont l'id est " + id + " n'existe pas");
        }
        return "redirect:/stations";
    }

}
