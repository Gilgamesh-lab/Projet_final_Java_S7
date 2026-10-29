package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Profil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// À poser sur une méthode (ou une classe) de contrôleur pour la réserver à certains profils.
// Exemple : @ReserveAuProfil(Profil.RESPONSABLE) sur l'affectation d'un prestataire (F7).
// Le contrôle est fait côté serveur par ConnexionInterceptor : un autre profil reçoit une 403.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReserveAuProfil {

    Profil[] value();
}
