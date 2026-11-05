package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Etat;
import altn72.tpfilrouge.modele.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {


    List<Equipement> findByType(String nom);
    void deleteEquipementByStation(Station idStation);

    @Modifying
    @Query("update Equipement u set u.reference = ?1, u.libelle = ?2, u.type = ?3, u.etat = ?4, u.station = ?5 where u.id = ?6")
    void setEquipementInfoById(String reference, String libelle, String type, Etat etat, Station station, Long idEquipement);
    // , , ,,
}

