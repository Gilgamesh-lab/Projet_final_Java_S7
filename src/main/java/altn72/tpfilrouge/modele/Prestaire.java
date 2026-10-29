package altn72.tpfilrouge.modele;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "prestaire")
public class Prestaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "identifiant", nullable = false, length = 100)
    private String identifiant;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Prestaire prestaire = (Prestaire) o;
        return Objects.equals(id, prestaire.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Prestaire{" +
                "id=" + id +
                ", identifiant='" + identifiant + '\'' +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}