package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Statut;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private PrestataireService prestataireService;

    @Autowired
    private Clock clock;

    public List<Incident> getIncidents() {
        return incidentRepository.findAll();
    }

    public List<Incident> getIncidentsResolue() {
        return incidentRepository.findByStatutOrderByDateResolutionDesc(Statut.RESOLU);
    }

    public Incident getIncidentByIdentifiant(String identifiant) {
        return incidentRepository.findByIdentifiant(identifiant);
    }

    public List<Incident> getIncidentsEquipement(Long idEquipement) {
        return incidentRepository.findByEquipementIdOrderByDateOuvertureDesc(idEquipement);
    }

    public Optional<Incident> getUnIncident(Long idIncident) {
        return incidentRepository.findById(idIncident);
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

    public boolean getNbIncidentIntervalle(int equipementId, String intervalle) {
        return incidentRepository.getNbIncidentIntervalle(equipementId, intervalle);
    }

    @Transactional
    public void modifierIncident(Long idIncident, Incident incidentModified) {
       if(incidentModified != null){
           incidentRepository.setIncidentInfoById(incidentModified.getIdentifiant(),
                   incidentModified.getEquipement(),incidentModified.getDescription(), incidentModified.getDateOuverture(), incidentModified.getStatut(),
                   incidentModified.getPrestataire(), incidentModified.getDateResolution(), incidentModified.getEvaluation(),
                   idIncident);
       }

    }
    // RG1 et RG2 : rattaché à un équipement, date d'ouverture renseignée, sans prestataire
    public Incident declarerIncident(Incident incident) {
        if (estVide(incident.getIdentifiant())) {
            throw new SaisieInvalideException("L'identifiant est obligatoire.");
        }
        if (incident.getEquipement() == null) {
            throw new SaisieInvalideException("Veuillez choisir un équipement.");
        }
        if (estVide(incident.getDescription())) {
            throw new SaisieInvalideException("La description est obligatoire.");
        }
        if (incidentRepository.findByIdentifiant(incident.getIdentifiant()) != null) {
            throw new SaisieInvalideException("Cet identifiant existe déjà, veuillez en choisir un autre.");
        }
        incident.setId(null);
        incident.setDateOuverture(LocalDateTime.now(clock));
        incident.setStatut(Statut.NOUVEAU);
        incident.setPrestataire(null);
        incident.setDateResolution(null);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident affecterPrestataire(Long idIncident, Long idPrestataire) {
        Incident incident = trouverIncident(idIncident);
        if (incident.getStatut() == Statut.RESOLU) {
            throw new RegleMetierException("Un incident résolu ne peut plus être affecté ni réaffecté.");
        }
        incident.setPrestataire(prestataireService.getPrestataireById(idPrestataire));
        incident.setStatut(Statut.EN_COURS);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident declarerResolu(Long idIncident) {
        Incident incident = trouverIncident(idIncident);
        if (incident.getStatut() == Statut.RESOLU) {
            throw new RegleMetierException("Cet incident est déjà résolu.");
        }
        resoudre(incident);
        return incidentRepository.save(incident);
    }

    // F6 : seule la description d'un incident non résolu est modifiable
    @Transactional
    public Incident modifierDescription(Long idIncident, String description) {
        Incident incident = trouverIncident(idIncident);
        verifierNonResolu(incident);
        changerDescription(incident, description);
        return incidentRepository.save(incident);
    }

    // API : description et passage à RESOLU uniquement, l'affectation d'un prestataire passe par F7
    @Transactional
    public Incident modifierDepuisApi(Long idIncident, String description, Statut statut) {
        Incident incident = trouverIncident(idIncident);
        verifierNonResolu(incident);
        if (statut != null && statut != Statut.RESOLU && statut != incident.getStatut()) {
            throw new SaisieInvalideException("Le seul changement de statut possible est le passage à RESOLU.");
        }
        if (description != null) {
            changerDescription(incident, description);
        }
        if (statut == Statut.RESOLU) {
            resoudre(incident);
        }
        return incidentRepository.save(incident);
    }

    private Incident trouverIncident(Long idIncident) {
        return incidentRepository.findById(idIncident)
                .orElseThrow(() -> new RessourceIntrouvableException("L'incident dont l'id est " + idIncident + " n'existe pas"));
    }

    private void verifierNonResolu(Incident incident) {
        if (incident.getStatut() == Statut.RESOLU) {
            throw new RegleMetierException("Un incident résolu ne peut plus être modifié.");
        }
    }

    private void changerDescription(Incident incident, String description) {
        if (estVide(description)) {
            throw new SaisieInvalideException("La description est obligatoire.");
        }
        incident.setDescription(description);
    }

    private void resoudre(Incident incident) {
        incident.setStatut(Statut.RESOLU);
        incident.setDateResolution(LocalDateTime.now(clock));
    }

    private boolean estVide(String texte) {
        return texte == null || texte.isBlank();
    }
}
