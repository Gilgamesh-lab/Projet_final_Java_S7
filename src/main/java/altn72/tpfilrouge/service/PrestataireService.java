package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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
