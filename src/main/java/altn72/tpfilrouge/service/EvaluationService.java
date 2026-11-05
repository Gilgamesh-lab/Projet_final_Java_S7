package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class EvaluationService {

    public static final int NOTE_MIN = 1;
    public static final int NOTE_MAX = 5;

    private final EvaluationRepository evaluationRepository;
    private final Clock clock;

    public EvaluationService(EvaluationRepository evaluationRepository, Clock clock) {
        this.evaluationRepository = evaluationRepository;
        this.clock = clock;
    }

    public void verifierEvaluable(Incident incident) {
        if (incident.getStatut() != Statut.RESOLU) {
            throw new RegleMetierException("L'incident doit être résolu pour être évalué.");
        }
        if (incident.getPrestataire() == null) {
            throw new RegleMetierException("L'incident n'a été affecté à aucun prestataire.");
        }
        if (evaluationRepository.existsByIncidentId(incident.getId())) {
            throw new RegleMetierException("Cet incident a déjà été évalué.");
        }
    }

    // Vérification et enregistrement dans la même transaction : pas de double évaluation entre les deux
    @Transactional
    public Evaluation evaluer(Incident incident, Integer note, String commentaire) {
        verifierEvaluable(incident);
        if (note == null || note < NOTE_MIN || note > NOTE_MAX) {
            throw new SaisieInvalideException("La note doit être comprise entre " + NOTE_MIN + " et " + NOTE_MAX + ".");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setIncident(incident);
        evaluation.setNote(note);
        evaluation.setCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        evaluation.setDateEvaluation(LocalDateTime.now(clock));
        return evaluationRepository.save(evaluation);
    }
}
