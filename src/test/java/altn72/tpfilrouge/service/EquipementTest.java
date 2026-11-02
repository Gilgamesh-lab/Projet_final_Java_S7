package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.EquipementRepository;
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
public class EquipementTest {



    @Mock
    private EquipementRepository equipementRepository;

    @InjectMocks
    private  EquipementService equipementService;

    private Equipement equipement;

    @BeforeEach
    void setUp() {
        equipement = new Equipement();
        equipement.setId(1L);
        equipement.setStation(new Station());
        equipement.setLibelle("XCX");
    }

    @Test
    void testGetUneEquipement() {
        when(equipementRepository.findById(1L)).thenReturn(Optional.ofNullable(equipement));
        assertThat(equipementService.getUnEquipement(1L).get()).isEqualTo(equipement);
    }

    @Test
    void testGetEquipements() {
        when(equipementRepository.findAll()).thenReturn(List.of(equipement, equipement, equipement, equipement));
        assertThat(equipementService.getEquipements().size()).isEqualTo(4);
    }

    @Test
    void testSupprimerEquipement() {
        //when(equipementRepository.findById(1L)).thenReturn(Optional.ofNullable(equipement));
        when(equipementRepository.existsById(1L)).thenReturn(true);
        equipementService.supprimerEquipement(1L);
        verify(equipementRepository).deleteById(1L);
    }

    @Test
    void testAjouterEquipement() {
        equipementService.ajouterEquipement(equipement);
        verify(equipementRepository).save(equipement);
    }

    @Test
    void testModifierEquipement() {
        equipementService.modifierEquipement(equipement.getId(), equipement);
        verify(equipementRepository).setEquipementInfoById(equipement.getReference(), equipement.getLibelle(),equipement.getType(), equipement.getEtat(), equipement.getStation(), equipement.getId());
    }

}
