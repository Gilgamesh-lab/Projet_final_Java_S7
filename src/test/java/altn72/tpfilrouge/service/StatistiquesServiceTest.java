package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Indicateur;
import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatistiquesServiceTest {

    private final IncidentRepository incidentRepository = mock(IncidentRepository.class);
    private final EvaluationRepository evaluationRepository = mock(EvaluationRepository.class);
    private final TableauDeBordService tableauDeBordService = mock(TableauDeBordService.class);
    private final StatistiquesService service =
            new StatistiquesService(incidentRepository, evaluationRepository, tableauDeBordService);

    private IncidentRepository.NombreParStation station(String nom, long nombre) {
        return new IncidentRepository.NombreParStation() {
            public String getStation() { return nom; }
            public Long getNombre() { return nombre; }
        };
    }

    private EvaluationRepository.NoteParPrestataire note(String nom, double moyenne, long nombre) {
        return new EvaluationRepository.NoteParPrestataire() {
            public String getPrestataire() { return nom; }
            public Double getMoyenne() { return moyenne; }
            public Long getNombre() { return nombre; }
        };
    }

    @Test
    void actifsParStationGardeLOrdreDeLaRequete() {
        when(incidentRepository.compterActifsParStation())
                .thenReturn(List.of(station("Gare Centrale", 3), station("Université", 1)));

        DonneesGraphique donnees = service.getActifsParStation();

        assertThat(donnees.getLibelles()).containsExactly("Gare Centrale", "Université");
        assertThat(donnees.getValeurs()).containsExactly(3L, 1L);
    }

    @Test
    void repartitionContientLesQuatreIndicateursDansLOrdre() {
        Map<Indicateur, Long> compteurs = new EnumMap<>(Indicateur.class);
        compteurs.put(Indicateur.VERT, 2L);
        compteurs.put(Indicateur.ORANGE, 1L);
        compteurs.put(Indicateur.ROUGE, 0L);
        compteurs.put(Indicateur.PRIS_EN_CHARGE, 4L);
        when(tableauDeBordService.getCompteurs()).thenReturn(compteurs);

        DonneesGraphique donnees = service.getRepartitionParIndicateur();

        assertThat(donnees.getLibelles()).containsExactly("Vert", "Orange", "Rouge", "Pris en charge");
        assertThat(donnees.getValeurs()).containsExactly(2L, 1L, 0L, 4L);
    }

    @Test
    void noteMoyenneArrondieAUneDecimaleAvecLeNombreDEvaluations() {
        when(evaluationRepository.calculerNoteMoyenneParPrestataire())
                .thenReturn(List.of(note("Ascenseurs Dupont", 4.666, 3), note("MécaTrans", 2.0, 1)));

        DonneesGraphique donnees = service.getNoteMoyenneParPrestataire();

        assertThat(donnees.getLibelles()).containsExactly("Ascenseurs Dupont (3 éval.)", "MécaTrans (1 éval.)");
        assertThat(donnees.getValeurs()).containsExactly(4.7, 2.0);
    }

    @Test
    void aucuneEvaluationDonneUnGraphiqueVide() {
        when(evaluationRepository.calculerNoteMoyenneParPrestataire()).thenReturn(List.of());
        assertThat(service.getNoteMoyenneParPrestataire().isVide()).isTrue();
    }
}
