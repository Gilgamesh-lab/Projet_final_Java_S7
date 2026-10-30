package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableauDeBordServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    private final IncidentRepository incidentRepository = mock(IncidentRepository.class);
    private TableauDeBordService service;

    private Station gare;
    private Station aeroport;
    private Incident rouge;
    private Incident orange;
    private Incident vert;
    private Incident prisEnCharge;

    @BeforeEach
    void preparer() {
        Clock clock = Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE);
        service = new TableauDeBordService(incidentRepository, new IndicateurService(clock), clock);

        gare = station(1L);
        aeroport = station(2L);
        rouge = incident(gare, 72, null);
        orange = incident(aeroport, 50, null);
        vert = incident(gare, 5, null);
        prisEnCharge = incident(aeroport, 100, new Prestataire());

        // Ordre volontairement mélangé pour vérifier le tri
        when(incidentRepository.findActifs()).thenReturn(List.of(vert, prisEnCharge, rouge, orange));
    }

    private Station station(Long id) {
        Station station = new Station();
        station.setId(id);
        return station;
    }

    private Incident incident(Station station, long heures, Prestataire prestataire) {
        Equipement equipement = new Equipement();
        equipement.setStation(station);
        Incident incident = new Incident();
        incident.setEquipement(equipement);
        incident.setDateOuverture(MAINTENANT.minusHours(heures));
        incident.setStatut(prestataire == null ? Statut.NOUVEAU : Statut.EN_COURS);
        incident.setPrestataire(prestataire);
        return incident;
    }

    private List<Incident> incidents(List<LigneTableauDeBord> lignes) {
        return lignes.stream().map(LigneTableauDeBord::getIncident).toList();
    }

    @Test
    void lesIncidentsEnAttenteSontTriesDuPlusAncienAuPlusRecentPuisLesPrisEnCharge() {
        assertThat(incidents(service.getLignes(null, null))).containsExactly(rouge, orange, vert, prisEnCharge);
    }

    @Test
    void chaqueLigneAfficheSonIndicateurEtSaDuree() {
        LigneTableauDeBord premiere = service.getLignes(null, null).getFirst();
        assertThat(premiere.getIndicateur()).isEqualTo(Indicateur.ROUGE);
        assertThat(premiere.getHeuresDepuisOuverture()).isEqualTo(72);
    }

    @Test
    void filtreParStation() {
        assertThat(incidents(service.getLignes(gare.getId(), null))).containsExactly(rouge, vert);
    }

    @Test
    void filtreParNiveau() {
        assertThat(incidents(service.getLignes(null, Indicateur.ORANGE))).containsExactly(orange);
    }

    @Test
    void filtresCombines() {
        assertThat(service.getLignes(aeroport.getId(), Indicateur.ROUGE)).isEmpty();
        assertThat(incidents(service.getLignes(aeroport.getId(), Indicateur.PRIS_EN_CHARGE))).containsExactly(prisEnCharge);
    }

    @Test
    void compteursParIndicateur() {
        Map<Indicateur, Long> compteurs = service.getCompteurs();
        assertThat(compteurs).containsEntry(Indicateur.ROUGE, 1L)
                .containsEntry(Indicateur.ORANGE, 1L)
                .containsEntry(Indicateur.VERT, 1L)
                .containsEntry(Indicateur.PRIS_EN_CHARGE, 1L);
    }

    @Test
    void compteursAZeroQuandAucunIncidentActif() {
        when(incidentRepository.findActifs()).thenReturn(List.of());
        assertThat(service.getCompteurs()).hasSize(4).allSatisfy((indicateur, nombre) -> assertThat(nombre).isZero());
    }
}
