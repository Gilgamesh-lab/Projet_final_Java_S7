package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsByIncidentId(Long incidentId);

    @Query("SELECT p.nom AS prestataire, AVG(e.note) AS moyenne, COUNT(e) AS nombre " +
            "FROM Evaluation e JOIN e.incident i JOIN i.prestataire p " +
            "GROUP BY p.id, p.nom ORDER BY AVG(e.note) DESC")
    List<NoteParPrestataire> calculerNoteMoyenneParPrestataire();

    interface NoteParPrestataire {
        String getPrestataire();
        Double getMoyenne();
        Long getNombre();
    }
}
