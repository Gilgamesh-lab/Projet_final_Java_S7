package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.ConnexionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

// F1 -- Connexion et déconnexion
@Controller
public class ConnexionControleur {

    // Injection de dépendance via le constructeur -- DÉBUT //
    private final ConnexionService connexionService;
    public ConnexionControleur(ConnexionService connexionService) { this.connexionService = connexionService; }
    // Injection de dépendance via le constructeur -- FIN //

    @GetMapping("/")
    public String afficherAccueil() {
        return "pageAccueil";
    }

    @GetMapping("/login")
    public String afficherConnexion(HttpSession session) {
        if (session.getAttribute(UtilisateurConnecte.ATTRIBUT_SESSION) != null) {
            return "redirect:/";
        }
        return "pageConnexion";
    }

    @PostMapping("/login")
    public String connecter(@RequestParam(defaultValue = "") String login,
                            @RequestParam(defaultValue = "") String motDePasse,
                            HttpServletRequest request, Model model) {
        Optional<UtilisateurConnecte> utilisateur = connexionService.authentifier(login, motDePasse);

        // Échec : on réaffiche la même page avec un message, le login saisi est conservé

        if (utilisateur.isEmpty()) {
            System.out.println("ok");
            model.addAttribute("erreur", "Login ou mot de passe incorrect.");
            model.addAttribute("login", login);
            return "pageConnexion";
        }

        HttpSession session = request.getSession();
        request.changeSessionId();    // nouvel identifiant de session après connexion (fixation de session)
        session.setAttribute(UtilisateurConnecte.ATTRIBUT_SESSION, utilisateur.get());
        return "redirect:/";
    }

    // Lien « Déconnectez-vous » du bandeau : fermeture de la session puis retour à la page de connexion
    @GetMapping("/logout")
    public String deconnecter(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
