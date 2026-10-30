package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    public List<Incident> getIncidents() {
        return incidentRepository.findAll();
    }

    public Optional<Incident> getUnIncident(Long idIncident) {
        Optional<Incident> uneIncident = incidentRepository.findById(idIncident);
        if (uneIncident.isEmpty()) System.out.println("Cet incident n'existe pas");
            return uneIncident;
    }

    @Transactional
    public boolean supprimerIncident(Long idIncident) {
        if (!incidentRepository.existsById(idIncident)) return false;
        incidentRepository.deleteById(idIncident);
        return true;
    }

    public Incident ajouterIncident(Incident incident) {
        incident.setId(null);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Optional<Incident> modifierIncident(Long idIncident, Incident incidentModified) {
       return incidentRepository.findById(idIncident).map(incidentToModify -> {
            BeanUtils.copyProperties(incidentModified, incidentToModify, "id");
            return incidentToModify;
        });
    }
}
