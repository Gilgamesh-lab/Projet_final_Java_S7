package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FragiliteServiceTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);
    private static final Long EQUIPEMENT = 1L;
    private static final Long VOISIN = 2L;

    @Mock
    private IncidentRepository incidentRepository;

    private FragiliteService service;
    private List<Incident> incidents;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new FragiliteService(incidentRepository, clock);
        incidents = new ArrayList<>();
        when(incidentRepository.findAll()).thenReturn(incidents);
    }

    private void ajouterIncident(Long idEquipement, LocalDateTime ouverture, Statut statut) {
        Equipement equipement = new Equipement();
        equipement.setId(idEquipement);
        Incident incident = new Incident();
        incident.setEquipement(equipement);
        incident.setDateOuverture(ouverture);
        incident.setStatut(statut);
        incidents.add(incident);
    }

    private void ajouterIncidentRecent(Long idEquipement) {
        ajouterIncident(idEquipement, MAINTENANT.minusDays(2), Statut.NOUVEAU);
    }

    @Test
    void deuxIncidentsDansLaFenetreNonFragile() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        assertThat(service.estFragile(EQUIPEMENT)).isFalse();
    }

    @Test
    void troisIncidentsDansLaFenetreFragile() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        assertThat(service.estFragile(EQUIPEMENT)).isTrue();
    }

    @Test
    void tousLesStatutsComptent() {
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(2), Statut.NOUVEAU);
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(3), Statut.EN_COURS);
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(4), Statut.RESOLU);
        assertThat(service.estFragile(EQUIPEMENT)).isTrue();
    }

    @Test
    void incidentOuvertIlYATrenteJoursEtUneMinuteNonCompte() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(30).minusMinutes(1), Statut.NOUVEAU);
        assertThat(service.estFragile(EQUIPEMENT)).isFalse();
    }

    @Test
    void incidentOuvertIlYAVingtNeufJoursVingtTroisHeuresCinquanteNeufCompte() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(29).minusHours(23).minusMinutes(59), Statut.NOUVEAU);
        assertThat(service.estFragile(EQUIPEMENT)).isTrue();
    }

    @Test
    void incidentOuvertIlYATrenteJoursPileCompte() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(30), Statut.NOUVEAU);
        assertThat(service.estFragile(EQUIPEMENT)).isTrue();
    }

    @Test
    void incidentsTropAnciensNonComptes() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        for (int i = 0; i < 5; i++) {
            ajouterIncident(EQUIPEMENT, MAINTENANT.minusDays(45), Statut.RESOLU);
        }
        assertThat(service.estFragile(EQUIPEMENT)).isFalse();
    }

    @Test
    void equipementVoisinNonContamine() {
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(EQUIPEMENT);
        ajouterIncidentRecent(VOISIN);
        assertThat(service.estFragile(EQUIPEMENT)).isTrue();
        assertThat(service.estFragile(VOISIN)).isFalse();
        assertThat(service.getEquipementsFragiles()).containsExactly(EQUIPEMENT);
    }
}
