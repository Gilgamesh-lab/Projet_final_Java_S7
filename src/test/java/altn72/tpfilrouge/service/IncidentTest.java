package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IncidentTest {



    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private PrestataireService prestataireService;

    @Spy
    private Clock clock = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @InjectMocks
    private  IncidentService incidentService;

    private Incident incident;

    @BeforeEach
    void setUp() {
        incident = new Incident();
        incident.setId(1L);
    }

    @Test
    void testGetUneIncident() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.ofNullable(incident));
        assertThat(incidentService.getUnIncident(1L).get()).isEqualTo(incident);
    }

    @Test
    void testGetIncidents() {
        when(incidentRepository.findAll()).thenReturn(List.of(incident, incident, incident, incident));
        assertThat(incidentService.getIncidents().size()).isEqualTo(4);
    }

    @Test
    void testSupprimerIncident() {
        when(incidentRepository.existsById(1L)).thenReturn(true);
        incidentService.supprimerIncident(1L);
        verify(incidentRepository).deleteById(1L);
    }

    @Test
    void testAjouterIncident() {
        incidentService.ajouterIncident(incident);
        verify(incidentRepository).save(incident);
    }

    @Test
    void testModifierIncident() {
        incidentService.modifierIncident(incident.getId(), incident);
        verify(incidentRepository).setIncidentInfoById(incident.getIdentifiant(),
                incident.getEquipement(),incident.getDescription(), incident.getDateOuverture(), incident.getStatut(),
                incident.getPrestataire(), incident.getDateResolution(), incident.getEvaluation(),
                incident.getId());
    }

    private Incident incidentADeclarer() {
        Incident nouveau = new Incident();
        nouveau.setIdentifiant("INC-100");
        nouveau.setEquipement(new Equipement());
        nouveau.setDescription("Panne");
        return nouveau;
    }

    @Test
    void testDeclarerIncident() {
        Incident nouveau = incidentADeclarer();
        nouveau.setPrestataire(new Prestataire());
        incidentService.declarerIncident(nouveau);
        verify(incidentRepository).save(nouveau);
        assertThat(nouveau.getStatut()).isEqualTo(Statut.NOUVEAU);
        assertThat(nouveau.getDateOuverture()).isEqualTo(MAINTENANT);
        assertThat(nouveau.getPrestataire()).isNull();
    }

    @Test
    void testDeclarerIncidentDescriptionVideRefuse() {
        Incident nouveau = incidentADeclarer();
        nouveau.setDescription("   ");
        assertThatThrownBy(() -> incidentService.declarerIncident(nouveau)).isInstanceOf(SaisieInvalideException.class);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void testDeclarerIncidentSansEquipementRefuse() {
        Incident nouveau = incidentADeclarer();
        nouveau.setEquipement(null);
        assertThatThrownBy(() -> incidentService.declarerIncident(nouveau)).isInstanceOf(SaisieInvalideException.class);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void testDeclarerIncidentIdentifiantExistantRefuse() {
        Incident nouveau = incidentADeclarer();
        when(incidentRepository.findByIdentifiant("INC-100")).thenReturn(incident);
        assertThatThrownBy(() -> incidentService.declarerIncident(nouveau)).isInstanceOf(SaisieInvalideException.class);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void testAffecterPrestataire() {
        Prestataire prestataire = new Prestataire();
        incident.setStatut(Statut.NOUVEAU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(prestataireService.getPrestataireById(2L)).thenReturn(prestataire);
        incidentService.affecterPrestataire(1L, 2L);
        assertThat(incident.getPrestataire()).isEqualTo(prestataire);
        assertThat(incident.getStatut()).isEqualTo(Statut.EN_COURS);
    }

    @Test
    void testAffecterIncidentResoluRefuse() {
        incident.setStatut(Statut.RESOLU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> incidentService.affecterPrestataire(1L, 2L)).isInstanceOf(RegleMetierException.class);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void testAffecterIncidentInconnuRefuse() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> incidentService.affecterPrestataire(1L, 2L)).isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void testDeclarerResolu() {
        incident.setStatut(Statut.EN_COURS);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        incidentService.declarerResolu(1L);
        assertThat(incident.getStatut()).isEqualTo(Statut.RESOLU);
        assertThat(incident.getDateResolution()).isEqualTo(MAINTENANT);
    }

    @Test
    void testDeclarerResoluDejaResoluRefuse() {
        incident.setStatut(Statut.RESOLU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> incidentService.declarerResolu(1L)).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void testModifierDescriptionVideRefuse() {
        incident.setStatut(Statut.NOUVEAU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> incidentService.modifierDescription(1L, " ")).isInstanceOf(SaisieInvalideException.class);
    }

    @Test
    void testModifierDescriptionIncidentResoluRefuse() {
        incident.setStatut(Statut.RESOLU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> incidentService.modifierDescription(1L, "Texte")).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void testApiPassageAResolu() {
        incident.setStatut(Statut.NOUVEAU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        incidentService.modifierDepuisApi(1L, null, Statut.RESOLU);
        assertThat(incident.getStatut()).isEqualTo(Statut.RESOLU);
        assertThat(incident.getDateResolution()).isEqualTo(MAINTENANT);
    }

    @Test
    void testApiAutreChangementDeStatutRefuse() {
        incident.setStatut(Statut.NOUVEAU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> incidentService.modifierDepuisApi(1L, null, Statut.EN_COURS)).isInstanceOf(SaisieInvalideException.class);
    }

    @Test
    void testApiStatutInchangeAccepte() {
        incident.setStatut(Statut.NOUVEAU);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        incidentService.modifierDepuisApi(1L, "Texte", Statut.NOUVEAU);
        assertThat(incident.getDescription()).isEqualTo("Texte");
        assertThat(incident.getStatut()).isEqualTo(Statut.NOUVEAU);
    }

}
