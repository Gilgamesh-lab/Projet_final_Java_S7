package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TableauDeBordService {

    private final IncidentRepository incidentRepository;
    private final IndicateurService indicateurService;
    private final Clock clock;

    public TableauDeBordService(IncidentRepository incidentRepository, IndicateurService indicateurService, Clock clock) {
        this.incidentRepository = incidentRepository;
        this.indicateurService = indicateurService;
        this.clock = clock;
    }

    // stationId et niveau sont facultatifs : null = pas de filtre
    public List<LigneTableauDeBord> getLignes(Long stationId, Indicateur niveau) {
        LocalDateTime maintenant = LocalDateTime.now(clock);
        return incidentRepository.findActifs().stream()
                .filter(incident -> stationId == null || stationId.equals(incident.getEquipement().getStation().getId()))
                .map(incident -> new LigneTableauDeBord(
                        incident,
                        indicateurService.calculer(incident),
                        Duration.between(incident.getDateOuverture(), maintenant).toHours()))
                .filter(ligne -> niveau == null || ligne.getIndicateur() == niveau)
                // Les incidents en attente d'affectation d'abord, du plus ancien au plus récent
                .sorted(Comparator.comparing((LigneTableauDeBord ligne) -> ligne.getIndicateur() == Indicateur.PRIS_EN_CHARGE)
                        .thenComparing(ligne -> ligne.getIncident().getDateOuverture()))
                .toList();
    }

    public Map<Indicateur, Long> getCompteurs() {
        Map<Indicateur, Long> compteurs = new EnumMap<>(Indicateur.class);
        for (Indicateur indicateur : Indicateur.values()) {
            compteurs.put(indicateur, 0L);
        }
        for (Incident incident : incidentRepository.findActifs()) {
            compteurs.merge(indicateurService.calculer(incident), 1L, Long::sum);
        }
        return compteurs;
    }

    public List<IncidentRepository.NombreParStation> getActifsParStation() {
        return incidentRepository.compterActifsParStation();
    }
}
