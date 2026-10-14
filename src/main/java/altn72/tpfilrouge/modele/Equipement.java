package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "equipement")
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipement", nullable = false)
    private Integer id;

    @Column(name = "description", nullable = true, length = 250)
    private String description;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @OneToOne
    @JoinColumn(name = "id_station", nullable = false)
    private Station station;

    @Column(name = "etat_fonctionnement", nullable = false, length = 100)
    private Boolean etatFonctionnement;

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
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", station=" + station +
                ", etatFonctionnement=" + etatFonctionnement +
                '}';
    }
}
