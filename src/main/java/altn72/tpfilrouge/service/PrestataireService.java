package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class PrestataireService {


    @Autowired
    private PrestataireRepository prestataireRepository;

    public List<Prestataire> getPrestataires() {
        return prestataireRepository.findAll();
    }

    public Prestataire getPrestataireById(Long id) {
        return prestataireRepository.findById(id).get();
    }



}
