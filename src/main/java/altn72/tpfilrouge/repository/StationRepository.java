package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByNom(String nom);


    @Modifying
    @Query("update Station u set u.nom = ?1, u.identifiant = ?2 where u.id = ?3")
    void setStationInfoById(String nom, String identifiant, Long id);
}

