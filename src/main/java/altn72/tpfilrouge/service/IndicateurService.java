package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.modele.Statut;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class IndicateurService {

    public static final int SEUIL_ORANGE_HEURES = 48;
    public static final int SEUIL_ROUGE_HEURES = 72;

    private final Clock clock;

    public IndicateurService(Clock clock) {
        this.clock = clock;
    }

    // Calculé à chaque appel à partir de l'heure courante, jamais stocké en base
    public Indicateur calculer(Incident incident) {
        if (incident.getStatut() == Statut.RESOLU) {
            throw new IllegalArgumentException("Un incident résolu n'a pas d'indicateur");
        }
        if (incident.getPrestataire() != null) {
            return Indicateur.PRIS_EN_CHARGE;
        }

        Duration duree = Duration.between(incident.getDateOuverture(), LocalDateTime.now(clock));
        if (duree.compareTo(Duration.ofHours(SEUIL_ROUGE_HEURES)) >= 0) {
            return Indicateur.ROUGE;
        }
        if (duree.compareTo(Duration.ofHours(SEUIL_ORANGE_HEURES)) >= 0) {
            return Indicateur.ORANGE;
        }
        return Indicateur.VERT;
    }
}
