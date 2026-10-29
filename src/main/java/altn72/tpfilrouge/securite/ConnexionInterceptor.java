package altn72.tpfilrouge.securite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

// Exécuté avant chaque contrôleur (sauf les routes exclues dans SecuriteConfig)
public class ConnexionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        UtilisateurConnecte utilisateur = session == null ? null
                : (UtilisateurConnecte) session.getAttribute(UtilisateurConnecte.ATTRIBUT_SESSION);

        // Pas connecté : renvoi vers la page de connexion (401 pour l'API, un client REST ne suit pas une page HTML)
        if (utilisateur == null) {
            if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }

        // Connecté mais profil non autorisé pour cette route : 403
        if (handler instanceof HandlerMethod methode) {
            ReserveAuProfil restriction = methode.getMethodAnnotation(ReserveAuProfil.class);
            if (restriction == null) {
                restriction = methode.getBeanType().getAnnotation(ReserveAuProfil.class);
            }
            if (restriction != null && Arrays.stream(restriction.value()).noneMatch(p -> p == utilisateur.getProfil())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès réservé au profil " + Arrays.toString(restriction.value()));
                return false;
            }
        }
        return true;
    }
}
