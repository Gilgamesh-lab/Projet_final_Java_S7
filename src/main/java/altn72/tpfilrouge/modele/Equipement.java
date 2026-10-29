package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "equipement")
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "reference", nullable = false, length = 100)
    private String reference;

    @Column(name = "libelle", nullable = false, length = 255)
    private String description;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "etat", nullable = false)
    private Etat etat;

    @OneToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Equipement that = (Equipement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", etat=" + etat +
                ", station=" + station +
                '}';
    }
}
