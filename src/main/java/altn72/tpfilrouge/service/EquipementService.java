package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class EquipementService {


    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    public List<Equipement> getEquipements() {
        return equipementRepository.findAll();
    }

    public Optional<Equipement> getUnEquipement(Long idEquipement) {
        return equipementRepository.findById(idEquipement);
    }

    @Transactional
    public boolean supprimerEquipement(Long idEquipement) {
        if (!equipementRepository.existsById(idEquipement)) return false;
        if (incidentRepository.existsByEquipementId(idEquipement)) {
            throw new RegleMetierException("Cet équipement a des incidents : il ne peut pas être supprimé.");
        }
        equipementRepository.deleteById(idEquipement);
        return true;
    }

    public Equipement ajouterEquipement(Equipement equipement) {
        equipement.setId(null);
        return equipementRepository.save(equipement);
    }

    @Transactional
    public Optional<Equipement> modifierEquipement(Long idEquipement, Equipement equipementModified) {
        equipementRepository.setEquipementInfoById(equipementModified.getReference(), equipementModified.getLibelle(),equipementModified.getType(), equipementModified.getEtat(), equipementModified.getStation(), equipementModified.getId());
        return equipementRepository.findById(idEquipement).map(equipementToModify -> {
            BeanUtils.copyProperties(equipementModified, equipementToModify, "id");
            return equipementToModify;
        });
    }
}
