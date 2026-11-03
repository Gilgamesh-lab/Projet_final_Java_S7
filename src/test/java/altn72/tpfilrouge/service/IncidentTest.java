package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IncidentTest {



    @Mock
    private IncidentRepository incidentRepository;

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
        //when(incidentRepository.findById(1L)).thenReturn(Optional.ofNullable(incident));
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
        when(incidentRepository.findById(1L)).thenReturn(Optional.ofNullable(incident));
        incidentService.modifierIncident(incident.getId(), incident);
        verify(incidentRepository).setIncidentInfoById(incident.getIdentifiant(),
                incident.getEquipement(),incident.getDescription(), incident.getDateOuverture(), incident.getStatut(),
                incident.getPrestataire(), incident.getDateResolution(), incident.getEvaluation(),
                incident.getId());
    }

}
