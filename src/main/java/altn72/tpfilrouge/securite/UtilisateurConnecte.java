package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;

import java.io.Serializable;

// Ce qu'on garde en session : uniquement le nécessaire, jamais le mot de passe haché
public class UtilisateurConnecte implements Serializable {

    // Nom de l'attribut de session, utilisé aussi dans le bandeau : ${session.utilisateur}
    public static final String ATTRIBUT_SESSION = "utilisateur";

    private final Long id;
    private final String prenom;
    private final Profil profil;

    public UtilisateurConnecte(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.prenom = utilisateur.getPrenom();
        this.profil = utilisateur.getProfil();
    }

    public Long getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    public Profil getProfil() {
        return profil;
    }

    public boolean isResponsable() {
        return profil == Profil.Responsable;
    }
}
