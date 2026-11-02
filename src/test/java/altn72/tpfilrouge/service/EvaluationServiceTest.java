package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluationServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
    private final EvaluationService service =
            new EvaluationService(evaluationRepository, Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE));

    private Incident incident;

    @BeforeEach
    void preparer() {
        incident = new Incident();
        incident.setId(10L);
        incident.setStatut(Statut.RESOLU);
        incident.setPrestataire(new Prestataire());
        when(evaluationRepository.existsByIncidentId(10L)).thenReturn(false);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void incidentResoluEtAffecteEstEvalue() {
        Evaluation evaluation = service.evaluer(incident, 4, "  Intervention rapide  ");

        assertThat(evaluation.getIncident()).isEqualTo(incident);
        assertThat(evaluation.getNote()).isEqualTo(4);
        assertThat(evaluation.getCommentaire()).isEqualTo("Intervention rapide");
        assertThat(evaluation.getDateEvaluation()).isEqualTo(MAINTENANT);
    }

    @Test
    void commentaireVideEnregistreCommeAbsent() {
        assertThat(service.evaluer(incident, 3, "   ").getCommentaire()).isNull();
    }

    @Test
    void incidentNonResoluRefuse() {
        incident.setStatut(Statut.EN_COURS);
        assertThatThrownBy(() -> service.evaluer(incident, 4, "")).isInstanceOf(IllegalStateException.class);
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void incidentSansPrestataireRefuse() {
        incident.setPrestataire(null);
        assertThatThrownBy(() -> service.evaluer(incident, 4, "")).isInstanceOf(IllegalStateException.class);
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void uneSeuleEvaluationParIncident() {
        when(evaluationRepository.existsByIncidentId(10L)).thenReturn(true);
        assertThatThrownBy(() -> service.evaluer(incident, 4, "")).isInstanceOf(IllegalStateException.class);
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void noteHorsDeLIntervalleRefusee() {
        assertThatThrownBy(() -> service.evaluer(incident, 0, "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.evaluer(incident, 6, "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.evaluer(incident, null, "")).isInstanceOf(IllegalArgumentException.class);
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void notesLimites1Et5Acceptees() {
        assertThat(service.evaluer(incident, 1, "").getNote()).isEqualTo(1);
        assertThat(service.evaluer(incident, 5, "").getNote()).isEqualTo(5);
    }
}
