package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Station;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {


    List<Equipement> findByType(String nom);
    void deleteEquipementByStation(Station idStation);
}

