package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

enum Profil{
    Responsable_exploitation, Agent
}

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "identifiant", nullable = false, length = 100)
    private String identifiant;


    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String mot_de_passe;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "profil", nullable = false, length = 100)
    private Profil profil;



}
