package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatutOrderByDateResolutionDesc(Statut statut);


    // JOIN FETCH : équipement, station et prestataire chargés en une seule requête au lieu d'une par incident
    @Query(value = "SELECT i FROM Incident i JOIN FETCH i.equipement e JOIN FETCH e.station LEFT JOIN FETCH i.prestataire " +
            "WHERE i.statut <> altn72.tpfilrouge.modele.Statut.RESOLU ORDER BY i.dateOuverture")
    List<Incident> findActifs();


    @Query(value = "SELECT s.nom AS station, COUNT(i.id) AS nombre FROM station s " +
            "JOIN equipement e ON e.station_id = s.id " +
            "JOIN incident i ON i.equipement_id = e.id " +
            "WHERE i.statut <> 'RESOLU' " +
            "GROUP BY s.id, s.nom ORDER BY nombre DESC",
            nativeQuery = true)
    List<NombreParStation> compterActifsParStation();



    interface NombreParStation {
        String getStation();
        Long getNombre();
    }

    @Modifying
    @Query("update Incident u set u.identifiant = ?1, u.equipement = ?2, u.description = ?3, u.dateOuverture = ?4, u.statut = ?5" +
            ", u.prestataire = ?6, u.dateResolution = ?7, u.evaluation = ?8 where u.id = ?9")
    void setIncidentInfoById(String identifiant, Equipement equipement, String description, LocalDateTime dateOuverture,
                               Statut statut, Prestataire prestataire, LocalDateTime dateResolution, Evaluation evaluation, Long id);
}
