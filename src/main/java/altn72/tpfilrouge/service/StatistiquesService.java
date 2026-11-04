package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class StatistiquesService {

    private final IncidentRepository incidentRepository;
    private final EvaluationRepository evaluationRepository;
    private final TableauDeBordService tableauDeBordService;

    public StatistiquesService(IncidentRepository incidentRepository, EvaluationRepository evaluationRepository,
                               TableauDeBordService tableauDeBordService) {
        this.incidentRepository = incidentRepository;
        this.evaluationRepository = evaluationRepository;
        this.tableauDeBordService = tableauDeBordService;
    }

    public DonneesGraphique getActifsParStation() {
        List<IncidentRepository.NombreParStation> lignes = incidentRepository.compterActifsParStation();
        return new DonneesGraphique(
                lignes.stream().map(IncidentRepository.NombreParStation::getStation).toList(),
                lignes.stream().<Number>map(IncidentRepository.NombreParStation::getNombre).toList());
    }

    public DonneesGraphique getRepartitionParIndicateur() {
        Map<Indicateur, Long> compteurs = tableauDeBordService.getCompteurs();
        List<Indicateur> indicateurs = Arrays.asList(Indicateur.values());
        return new DonneesGraphique(
                indicateurs.stream().map(Indicateur::getLibelle).toList(),
                indicateurs.stream().<Number>map(compteurs::get).toList());
    }

    public DonneesGraphique getNoteMoyenneParPrestataire() {
        List<EvaluationRepository.NoteParPrestataire> lignes = evaluationRepository.calculerNoteMoyenneParPrestataire();
        return new DonneesGraphique(
                lignes.stream().map(ligne -> ligne.getPrestataire() + " (" + ligne.getNombre() + " éval.)").toList(),
                lignes.stream().<Number>map(ligne -> Math.round(ligne.getMoyenne() * 10) / 10.0).toList());
    }
}
