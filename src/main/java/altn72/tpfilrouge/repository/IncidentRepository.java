package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Statut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    void deleteIncidentByEquipement_Id(Long equipementId);

    interface NombreParStation {
        String getStation();
        Long getNombre();
    }
}
