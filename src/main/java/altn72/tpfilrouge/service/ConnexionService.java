package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.UtilisateurRepository;
import altn72.tpfilrouge.securite.UtilisateurConnecte;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConnexionService {

    // Injection de dépendance via le constructeur -- DÉBUT //
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    public ConnexionService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // Injection de dépendance via le constructeur -- FIN //

    // Vide si le login n'existe pas ou si le mot de passe ne correspond pas au haché :
    // on ne dit pas lequel des deux est faux
    public Optional<UtilisateurConnecte> authentifier(String identifiant, String motDePasse) {
        return utilisateurRepository.findByIdentifiant(identifiant)
                .filter(utilisateur -> passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse()))
                .map(UtilisateurConnecte::new);
    }
}
