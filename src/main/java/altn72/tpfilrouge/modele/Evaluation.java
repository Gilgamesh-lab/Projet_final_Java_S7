package altn72.tpfilrouge.modele;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @JoinColumn(name = "id_incident", nullable = false)
    private Integer id;



    @Column(name = "note", nullable = false, length = 100)
    private int note;

    @Column(name = "commentaires", nullable = false, length = 100)
    private String commentaires;

    @Column(name = "date", nullable = false, length = 100)
    private Date date;

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
                ", note=" + note +
                ", commentaires='" + commentaires + '\'' +
                ", date=" + date +
                '}';
    }
}