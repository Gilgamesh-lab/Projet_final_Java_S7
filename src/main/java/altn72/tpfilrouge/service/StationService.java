package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RegleMetierException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.SaisieInvalideException;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;


@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    public List<Station> getStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getUneStation(Long idStation) {
        return stationRepository.findById(idStation);
    }

    public Station getUneStation(String nomStation) {
        return stationRepository.findByNom(nomStation).stream().findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("La station " + nomStation + " n'existe pas"));
    }

    @Transactional
    public boolean supprimerStation(Long idStation) {
        if (!stationRepository.existsById(idStation)) return false;
        if (incidentRepository.existsByEquipementStationId(idStation)) {
            throw new RegleMetierException("Cette station a des équipements avec des incidents : elle ne peut pas être supprimée.");
        }
        equipementRepository.deleteEquipementByStation(this.getUneStation(idStation).get());
        stationRepository.deleteById(idStation);
        return true;
    }

    public Station ajouterStation(Station station) {
        verifierSaisie(station);
        station.setId(null);
        return stationRepository.save(station);
    }

    @Transactional
    public Optional<Station> modifierStation(Long idStation, Station stationModified) {
        verifierSaisie(stationModified);
        stationRepository.setStationInfoById(stationModified.getNom(), stationModified.getIdentifiant(), stationModified.getId());
       return stationRepository.findById(idStation).map(stationToModify -> {
            BeanUtils.copyProperties(stationModified, stationToModify, "id");
            return stationToModify;
        });
    }




    @Transactional
    public int importerJson(MultipartFile fichier) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream in = fichier.getInputStream()) {
            List<Station> stations = objectMapper.readValue(in, new TypeReference<List<Station>>() {});
            stations.forEach(station -> {
                verifierSaisie(station);
                station.setId(null);
            });
            stationRepository.saveAll(stations);
            return stations.size();
        }
    }

    private void verifierSaisie(Station station) {
        if (station.getNom() == null || station.getNom().isBlank()
                || station.getIdentifiant() == null || station.getIdentifiant().isBlank()) {
            throw new SaisieInvalideException("Le nom et l'identifiant d'une station sont obligatoires.");
        }
    }

}
