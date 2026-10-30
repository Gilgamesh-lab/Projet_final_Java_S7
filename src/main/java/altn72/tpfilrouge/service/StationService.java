package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public List<Station> getStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getUneStation(Long idStation) {
        Optional<Station> uneStation = stationRepository.findById(idStation);
        if (uneStation.isEmpty()) System.out.println("Cette station n'existe pas");
            return uneStation;
    }

    @Transactional
    public boolean supprimerStation(Long idStation) {
        if (!stationRepository.existsById(idStation)) return false;
        stationRepository.deleteById(idStation);
        return true;
    }

    public Station ajouterStation(Station station) {
        station.setId(null);
        return stationRepository.save(station);
    }

    @Transactional
    public Optional<Station> modifierstation(Long idStation, Station stationModified) {
       return stationRepository.findById(idStation).map(stationToModify -> {
            BeanUtils.copyProperties(stationModified, stationToModify, "id");
            return stationToModify;
        });
    }
}
