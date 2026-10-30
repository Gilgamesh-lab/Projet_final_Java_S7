package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Statut;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndicateurServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    private final IndicateurService service =
            new IndicateurService(Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE));

    private Incident incidentOuvertDepuis(long minutes, Statut statut, Prestataire prestataire) {
        Incident incident = new Incident();
        incident.setDateOuverture(MAINTENANT.minusMinutes(minutes));
        incident.setStatut(statut);
        incident.setPrestataire(prestataire);
        return incident;
    }

    private Incident nonAffecteOuvertDepuis(long heures, long minutes) {
        return incidentOuvertDepuis(heures * 60 + minutes, Statut.NOUVEAU, null);
    }

    @Test
    void incidentOuvertA47hEstVert() {
        assertThat(service.calculer(nonAffecteOuvertDepuis(47, 0))).isEqualTo(Indicateur.VERT);
    }

    @Test
    void incidentOuvertA47h59EstEncoreVert() {
        assertThat(service.calculer(nonAffecteOuvertDepuis(47, 59))).isEqualTo(Indicateur.VERT);
    }

    @Test
    void incidentOuvertA48hPileEstOrange() {
        assertThat(service.calculer(nonAffecteOuvertDepuis(48, 0))).isEqualTo(Indicateur.ORANGE);
    }

    @Test
    void incidentOuvertA71h59EstEncoreOrange() {
        assertThat(service.calculer(nonAffecteOuvertDepuis(71, 59))).isEqualTo(Indicateur.ORANGE);
    }

    @Test
    void incidentOuvertA72hPileEstRouge() {
        assertThat(service.calculer(nonAffecteOuvertDepuis(72, 0))).isEqualTo(Indicateur.ROUGE);
    }

    @Test
    void incidentAffecteEstPrisEnChargeMemeApres72h() {
        Incident incident = incidentOuvertDepuis(80 * 60, Statut.EN_COURS, new Prestataire());
        assertThat(service.calculer(incident)).isEqualTo(Indicateur.PRIS_EN_CHARGE);
    }

    @Test
    void incidentAffecteRecentEstPrisEnChargeEtPasVert() {
        Incident incident = incidentOuvertDepuis(5 * 60, Statut.EN_COURS, new Prestataire());
        assertThat(service.calculer(incident)).isEqualTo(Indicateur.PRIS_EN_CHARGE);
    }

    @Test
    void incidentResoluNaPasDIndicateur() {
        Incident incident = incidentOuvertDepuis(10 * 60, Statut.RESOLU, new Prestataire());
        assertThatThrownBy(() -> service.calculer(incident)).isInstanceOf(IllegalArgumentException.class);
    }
}
