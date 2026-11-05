package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StationTest {


    @Mock
    private StationRepository stationRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private  StationService stationService;

    private Station station;

    @BeforeEach
    void setUp() {
        station = new Station();
        station.setId(1L);
        station.setNom("Mira");
        station.setIdentifiant("XCX");
    }

    @Test
    void testGetUneStation() {
        when(stationRepository.findById(1L)).thenReturn(Optional.ofNullable(station));
        assertThat(stationService.getUneStation(1L).get()).isEqualTo(station);
    }

    @Test
    void testGetStations() {
        when(stationRepository.findAll()).thenReturn(List.of(station, station, station, station));
        assertThat(stationService.getStations().size()).isEqualTo(4);
    }

    @Test
    void testSupprimerStation() {
        when(stationRepository.findById(1L)).thenReturn(Optional.ofNullable(station));
        when(stationRepository.existsById(1L)).thenReturn(true);
        stationService.supprimerStation(1L);
        verify(stationRepository).deleteById(1L);
    }

    @Test
    void testSupprimerStationAvecIncidentsRefuse() {
        when(stationRepository.existsById(1L)).thenReturn(true);
        when(incidentRepository.existsByEquipementStationId(1L)).thenReturn(true);
        assertThatThrownBy(() -> stationService.supprimerStation(1L)).isInstanceOf(RegleMetierException.class);
        verify(stationRepository, never()).deleteById(1L);
    }

    @Test
    void testAjouterStationSansNomRefuse() {
        station.setNom(" ");
        assertThatThrownBy(() -> stationService.ajouterStation(station)).isInstanceOf(SaisieInvalideException.class);
        verify(stationRepository, never()).save(station);
    }

    @Test
    void testAjouterStation() {
        stationService.ajouterStation(station);
        verify(stationRepository).save(station);
    }

    @Test
    void testModifierStation() {
        stationService.modifierStation(station.getId(), station);
        verify(stationRepository).setStationInfoById(station.getNom(), station.getIdentifiant(),station.getId());
    }

}
