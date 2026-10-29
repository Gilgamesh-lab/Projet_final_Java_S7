package altn72.tpfilrouge.modele;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @JoinColumn(name = "id", nullable = false)
    private Integer id;

    @OneToOne
    @Column(name = "incident_id", nullable = false, length = 100)
    private Incident incident_id;

    @Column(name = "note", nullable = false, length = 100)
    private int note;

    @Column(name = "commentaire", nullable = false, length = 100)
    private String commentaire;

    @Column(name = "date_evaluation", nullable = false, length = 100)
    private Date date_evaluation;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", incident_id=" + incident_id +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", date_evaluation=" + date_evaluation +
                '}';
    }
}