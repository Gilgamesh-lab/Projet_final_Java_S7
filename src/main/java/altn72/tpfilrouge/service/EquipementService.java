package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.repository.EquipementRepository;
// import jakarta.transaction.Transactional;
// 4.6.2 -- On utilise le @Transactional de Spring (et non celui de Jakarta) : c'est celui que Spring gère nativement,
// et il offre plus d'options (readOnly, rollbackFor, propagation...).
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

    public List<Equipement> getEquipements() {
        return equipementRepository.findAll();
    }

    public Optional<Equipement> getUnEquipement(Long idEquipement) {
        Optional<Equipement> unEquipement = equipementRepository.findById(idEquipement);

        if (unEquipement.isEmpty()) System.out.println("Ce equipement n'existe pas");
            return unEquipement;
    }

    @Transactional
    public boolean supprimerEquipement(Long idEquipement) {
        if (!equipementRepository.existsById(idEquipement)) return false;
            equipementRepository.deleteById(idEquipement);
        return true;
    }

    public Equipement ajouterEquipement(Equipement equipement) {
        equipement.setId(null);
        return equipementRepository.save(equipement);
    }

    @Transactional
    public Optional<Equipement> modifierEquipement(Long idEquipement, Equipement equipementModified) {
        return equipementRepository.findById(idEquipement).map(equipementToModify -> {
            BeanUtils.copyProperties(equipementModified, equipementToModify, "id");
            return equipementToModify;
        });
    }
}
