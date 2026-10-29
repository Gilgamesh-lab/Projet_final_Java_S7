# Journal -- Supervision des équipements et suivi des incidents (ALTN72, 2026-27)

**Groupe :** Christophe HUANG, Fabrice AMEGADJEN

## README
- Prérequis : Java 25, Maven (wrapper `mvnw` fourni), MariaDB ou PostgreSQL.
- Configuration : la connexion à la base se règle dans `src/main/resources/application.properties`
  (ou un profil local non versionné, par exemple `application-christophe.properties` / `application-fabrice.properties`).
- Initialiser la base (MariaDB) : créer une base vide `Base_ALTN72` et ajouter `spring.sql.init.mode=always`
  à la configuration. Au démarrage, Spring exécute `src/main/resources/schema.sql` (tables) puis
  `src/main/resources/data.sql` (données de démonstration) : la base est recréée à chaque lancement.
- Lancer : `./mvnw spring-boot:run` puis ouvrir http://localhost:8080
- Application déployée : à compléter
- Comptes de test :
  - Responsable d'exploitation : `responsable` / `responsable123`
  - Agent : `agent` / `agent123`
- Données de démonstration (dates relatives à `NOW()`, donc toujours cohérentes avec l'heure courante) :
  - 4 stations, 8 équipements, 3 prestataires ;
  - incidents non affectés ouverts depuis 5 h, 47 h, 48 h, 71 h et 72 h (limites vert / orange / rouge) ;
  - 2 incidents en cours affectés à un prestataire, 3 incidents résolus dont 2 évalués et 1 à évaluer (F10).
- Limites connues : à compléter

## ARCHITECTURE
- Dépôt : https://github.com/Gilgamesh-lab/Projet_final_Java_S7
- Organisation en couches :
  - `modele` : entités JPA et repositories ;
  - `service` : règles métier ;
  - `controleur` : contrôleurs Spring MVC, vues Thymeleaf dans `src/main/resources/templates` ;
  - `securite` : connexion, protection des pages et des droits par profil.
- Schéma : à compléter

## TESTS
- Stratégie : à compléter
- Commande : `./mvnw verify`
- Couvert : à compléter | Non couvert : à compléter

## CHOIX TECHNIQUES
- Droits par profil :

| N°     | Fonctionnalité                   | Responsable d'exploitation | Agent                                 |
|--------|----------------------------------|----------------------------|---------------------------------------|
| F2-F3  | Consulter équipements et fiches  | Accès complet              | Accès complet                         |
| F4     | Déclarer un incident             | Accès complet              | Accès complet                         |
| F5-F6  | Consulter / modifier un incident | Accès complet              | Accès complet                         |
| F7     | Affecter ou réaffecter un prestataire | Accès complet         | Interdit                              |
| F8     | Déclarer un incident résolu      | Accès complet              | Interdit                              |
| F9     | Consulter l'historique           | Accès complet              | Lecture seule                         |
| F10    | Évaluer un prestataire           | Accès complet              | Interdit                              |
| F11    | Tableau de bord                  | Accès complet              | Interdit                              |

- Connexion (F1) et sécurité :
  - session HTTP simple plutôt que Spring Security complet : un intercepteur (`ConnexionInterceptor`) suffit pour
    deux profils et reste facile à expliquer ;
  - seul `spring-security-crypto` est utilisé, pour le hachage BCrypt des mots de passe (jamais stockés en clair, §7.7) ;
  - en session on garde un objet `UtilisateurConnecte` (id, prénom, profil), jamais le mot de passe haché ;
  - l'identifiant de session est régénéré après la connexion (fixation de session) ;
  - un utilisateur non connecté est redirigé vers `/login` ; pour l'API (`/api/**`) on renvoie 401,
    un client REST ne sait pas suivre une page HTML ;
  - les droits du tableau ci-dessus sont appliqués côté serveur avec l'annotation `@ReserveAuProfil` posée sur les
    méthodes des contrôleurs : un profil non autorisé reçoit une 403, même s'il tape l'URL à la main.
    Masquer les boutons dans les vues n'est qu'un confort en plus.
- Base de données :
  - les énumérations SQL (`etat`, `statut`, `profil`) ont exactement les noms des enums Java, ce qui permet
    `@Enumerated(EnumType.STRING)` sans convertisseur ;
  - RG8 (une seule évaluation par incident) : `UNIQUE` sur `evaluation.incident_id` ;
  - RG8 (note de 1 à 5) et RG9 (résolution après ouverture) : contraintes `CHECK` ;
  - l'indicateur vert / orange / rouge n'est pas stocké : il est calculé à partir de l'heure courante.
- Exceptions : à compléter
- SOLID : à compléter
- Requête SQL native : à compléter
- @Transactional : à compléter

## LOT ALPHA
### Fonctionnalité transverse 1 : à compléter
- Besoin · périmètre envisagé · choix et essais · ce qui fonctionne · difficultés · reste à faire
### Fonctionnalité transverse 2 : à compléter

## BILAN
- Contribution de chacun : à compléter
- Plus grande difficulté et comment elle a été surmontée : à compléter
- Fonctionnalités non réalisées et pourquoi : à compléter
- Trois points à retenir du cours et du projet : à compléter
