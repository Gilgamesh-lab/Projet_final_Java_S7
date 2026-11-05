package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FragiliteService {

    public static final int SEUIL_INCIDENTS = 3;
    public static final int FENETRE_JOURS = 30;

    private final IncidentRepository incidentRepository;
    private final Clock clock;

    public FragiliteService(IncidentRepository incidentRepository, Clock clock) {
        this.incidentRepository = incidentRepository;
        this.clock = clock;
    }

    public boolean estFragile(Long idEquipement) {
        return getEquipementsFragiles().contains(idEquipement);
    }

    // EF1 à EF4 : tous les statuts comptent et le résultat est recalculé à chaque consultation
    public Set<Long> getEquipementsFragiles() {
        LocalDateTime limite = LocalDateTime.now(clock).minusDays(FENETRE_JOURS);
        Map<Long, Long> incidentsParEquipement = incidentRepository.findAll().stream()
                .filter(incident -> !incident.getDateOuverture().isBefore(limite))
                .collect(Collectors.groupingBy(this::idEquipement, Collectors.counting()));
        return incidentsParEquipement.entrySet().stream()
                .filter(ligne -> ligne.getValue() >= SEUIL_INCIDENTS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Long idEquipement(Incident incident) {
        return incident.getEquipement().getId();
    }
}
