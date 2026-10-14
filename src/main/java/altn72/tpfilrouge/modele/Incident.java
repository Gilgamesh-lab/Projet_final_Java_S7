package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident", nullable = false)
    private Integer id;

    @OneToMany
    @JoinColumn(name = "id_equipement", nullable = false)
    private ArrayList<Equipement> equipement;

    @Column(name = "description", nullable = true, length = 250)
    private String description;

    @Column(name = "horaire_ouverture", nullable = false, length = 100)
    private LocalDateTime horaireOuverture;

    @Column(name = "statut", nullable = false, length = 100)
    private String statut;

    @ManyToOne
    @Column(name = "prestataire", nullable = true, length = 100)
    private Prestaire prestataire;

    @Column(name = "horaire_resolution", nullable = true, length = 100)
    private LocalDateTime horaireResolution;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Incident incident = (Incident) o;
        return Objects.equals(id, incident.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", equipement=" + equipement +
                ", description='" + description + '\'' +
                ", horaireOuverture=" + horaireOuverture +
                ", statut='" + statut + '\'' +
                ", prestataire=" + prestataire +
                ", horaireResolution=" + horaireResolution +
                '}';
    }
}
