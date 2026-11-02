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
  - avec un profil local : `./mvnw spring-boot:run -Dspring-boot.run.profiles=christophe` (MariaDB)
    ou `-Dspring-boot.run.profiles=fabrice` (PostgreSQL).
- Application déployée : à compléter
- Comptes de test :
  - Responsable d'exploitation : `responsable` / `responsable123`
  - Agent : `agent` / `agent123`
- Données de démonstration (dates relatives à `NOW()`, donc toujours cohérentes avec l'heure courante) :
  - 4 stations, 8 équipements, 3 prestataires ;
  - incidents non affectés ouverts depuis 5 h, 47 h, 48 h, 71 h et 72 h (limites vert / orange / rouge) ;
  - 2 incidents en cours affectés à un prestataire, 3 incidents résolus dont 2 évalués et 1 à évaluer (F10).
- Limites connues :
  - `schema.sql` est écrit pour MariaDB (`AUTO_INCREMENT`, `ENUM`, `UNSIGNED`) : il ne s'exécute pas tel quel sous PostgreSQL ;
  - la base est recréée à chaque démarrage : les données saisies pendant une démonstration sont perdues au redémarrage.

## ARCHITECTURE
- Dépôt : https://github.com/Gilgamesh-lab/Projet_final_Java_S7
- Organisation en couches :
  - `modele` : entités JPA et énumérations ;
  - `repository` : repositories Spring Data JPA (requêtes dérivées, JPQL et native) ;
  - `service` : règles métier ;
  - `controleur` : contrôleurs Spring MVC, vues Thymeleaf dans `src/main/resources/templates` ;
  - `securite` : connexion, protection des pages et des droits par profil.
- Schéma :

```
Navigateur
   │  requête HTTP
   ▼
ConnexionInterceptor ── non connecté ──► /login (401 pour /api/**)
   │  profil interdit ──► 403
   ▼
Contrôleurs (controleur) ──► vues Thymeleaf (templates, error/404 et error/403)
   │
   ▼
Services (service) ── règles métier, IndicateurService + Clock
   │
   ▼
Repositories Spring Data JPA (repository)
   │
   ▼
MariaDB (schema.sql + data.sql)
```

## TESTS
- Stratégie : les règles métier sont testées en tests unitaires JUnit, sans démarrer Spring ni la base, donc rapides
  et indépendants des données. L'heure est fixée grâce à une horloge (`Clock`) injectée dans les services, ce qui
  permet de tester précisément les seuils sans dépendre de l'heure réelle. Les repositories sont remplacés par des
  objets simulés (Mockito) : on teste la logique du service, pas la base.
- Commande : `./mvnw verify`
- Couvert (22 tests) :
  - calcul de l'indicateur d'urgence (`IndicateurServiceTest`, 8 tests) :
    - non affecté ouvert depuis 47 h et 47 h 59 : vert ;
    - 48 h pile et 71 h 59 : orange ;
    - 72 h pile : rouge ;
    - affecté à un prestataire (depuis 5 h ou 80 h) : « pris en charge », jamais vert / orange / rouge ;
    - incident résolu : pas d'indicateur.
  - tableau de bord (`TableauDeBordServiceTest`, 7 tests) : tri (incidents en attente du plus ancien au plus récent,
    puis les pris en charge), indicateur et durée de chaque ligne, filtre par station, par niveau, filtres combinés,
    compteurs par indicateur (y compris à zéro).
  - évaluation (`EvaluationServiceTest`, 7 tests) : évaluation d'un incident résolu et affecté, refus d'un incident
    non résolu, sans prestataire ou déjà évalué, refus des notes 0, 6 et vide, acceptation des limites 1 et 5 ;
    en cas de refus, on vérifie que rien n'est enregistré.
- Non couvert :
  - les contrôleurs et les pages (pas de tests MockMvc) : vérifiés à la main avec les deux comptes de test ;
  - les requêtes JPQL et native : pas de tests `@DataJpaTest`, il faudrait une base de test (H2 ou Testcontainers)
    compatible avec `schema.sql`. Elles ont été vérifiées en lançant l'application sur MariaDB avec `data.sql`.

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
- Indicateur d'urgence (RG3 à RG5) :
  - calculé dans `IndicateurService` à chaque affichage, à partir de la date d'ouverture et de l'heure courante ;
  - les seuils n'existent qu'à un seul endroit, dans les constantes `SEUIL_ORANGE_HEURES` (48) et `SEUIL_ROUGE_HEURES` (72) ;
  - la durée est comparée avec `Duration` plutôt qu'en heures entières, pour que 47 h 59 reste vert et que 48 h pile
    soit orange ;
  - un incident affecté à un prestataire prend l'état `PRIS_EN_CHARGE`, distinct des trois couleurs (RG5) ;
  - l'heure courante vient d'un `Clock` déclaré comme bean (`HorlogeConfig`) au lieu de `LocalDateTime.now()` :
    en test on injecte une horloge fixe.
- Tableau de bord (F11) :
  - `TableauDeBordService` associe à chaque incident actif son indicateur et sa durée d'ouverture
    (`LigneTableauDeBord`) ;
  - filtres facultatifs par station et par niveau (`null` = pas de filtre), combinables ;
  - tri : incidents en attente d'affectation du plus ancien au plus récent (donc les rouges en tête), puis les
    incidents pris en charge ;
  - compteurs par indicateur et nombre d'incidents actifs par station (requête native).
- Évaluation des prestataires (F10) :
  - règles vérifiées dans `EvaluationService` : incident résolu, affecté à un prestataire, pas encore évalué,
    note de 1 à 5 (constantes `NOTE_MIN` / `NOTE_MAX`) ;
  - double protection : le service renvoie un message clair, et la base refuse de toute façon les doublons
    (`UNIQUE`) et les notes invalides (`CHECK`) ;
  - réservée au responsable par `@ReserveAuProfil` sur `EvaluationControleur` ; la note est affichée dans l'historique.
- Exceptions : `ResponseStatusException` dans les contrôleurs plutôt qu'un `@ControllerAdvice`.
  - Nous n'avons que deux cas d'erreur HTTP : ressource inexistante (404) et profil non autorisé (403). Lever
    l'exception à l'endroit où l'erreur est détectée reste simple et lisible ; un `@ControllerAdvice` serait plus
    utile avec beaucoup d'exceptions métier différentes à traduire.
  - Spring Boot affiche automatiquement nos pages `templates/error/404.html` et `error/403.html`, avec le bandeau et
    le message du contrôleur (`spring.web.error.include-message=always`).
  - Les erreurs de saisie (note invalide, incident non évaluable) ne sont pas des erreurs HTTP : le service lève une
    exception métier (`IllegalArgumentException`, `IllegalStateException`), le contrôleur l'attrape et réaffiche le
    formulaire avec le message.
- SOLID :
  - Responsabilité unique (S) : `IndicateurService` ne fait que calculer l'indicateur, `ConnexionService` que vérifier
    les identifiants ; la protection des pages est isolée dans `ConnexionInterceptor` au lieu d'être répétée dans
    chaque contrôleur.
  - Inversion des dépendances (D) : les services dépendent d'abstractions injectées par Spring (`Clock`,
    `PasswordEncoder`, interfaces des repositories) et non d'implémentations concrètes. C'est ce qui permet de
    tester `IndicateurService` avec une horloge fixe, et on pourrait changer d'algorithme de hachage sans toucher
    à `ConnexionService`.
- Requête SQL native : `IncidentRepository.compterActifsParStation()`, nombre d'incidents non résolus par station,
  affiché sur le tableau de bord. Le résultat est lu à travers une interface de projection (`NombreParStation`) plutôt
  qu'un tableau `Object[]`.
  - Intérêt : un regroupement simple sur trois tables (`station`, `equipement`, `incident`), écrit exactement comme
    on l'écrirait en SQL, et calculé par la base sans charger tous les incidents en mémoire.
  - Limites :
    - elle n'est pas vérifiée au démarrage, contrairement aux requêtes JPQL : une erreur n'apparaît qu'à l'exécution ;
    - elle dépend des noms de tables et de colonnes, et plus seulement des entités Java : renommer un attribut ne la
      met pas à jour ;
    - elle dépend du SGBD : sous Linux, MariaDB distingue la casse des noms de tables. Nous l'avons constaté en cours
      de projet avec `FROM Station` au lieu de `FROM station`, qui fonctionnait sous Windows mais aurait échoué au
      déploiement ;
    - la même requête en JPQL (`SELECT e.station.nom, COUNT(i) FROM Incident i JOIN i.equipement e ... GROUP BY ...`)
      aurait été portable et vérifiée au démarrage : c'est ce que nous choisirions hors contrainte de l'énoncé.
- @Transactional :
  - `EvaluationService.evaluer` : on vérifie qu'il n'existe pas déjà d'évaluation, puis on l'insère. La transaction
    fait de ces deux étapes une seule opération : tout passe ou rien ne passe ;
  - méthodes de modification et de suppression des services (`modifierIncident`, `supprimerIncident`,
    `supprimerEquipement`, `supprimerStation`...) : lecture puis écriture dans une même unité de travail, et
    suppression de plusieurs données liées (station, équipements, incidents) de façon atomique (RG10) ;
  - pas de `@Transactional` sur les méthodes de simple lecture : il n'apporte rien sans écriture.

## LOT ALPHA
### Fonctionnalité transverse 1 : à compléter
- Besoin · périmètre envisagé · choix et essais · ce qui fonctionne · difficultés · reste à faire
### Fonctionnalité transverse 2 : à compléter

## BILAN
- Contribution de chacun :
  - Christophe HUANG : base de données (`schema.sql`, `data.sql`), alignement des entités sur le schéma, maquettes
    des pages, connexion / déconnexion et droits par profil (F1), calcul de l'indicateur d'urgence, service et
    requêtes du tableau de bord (F11), évaluation des prestataires (F10), pages d'erreur, tests unitaires, journal.md ;
  - Fabrice AMEGADJEN : entités JPA, stations (`StationRepository`, `StationService`, `StationControleur`),
    configuration PostgreSQL, alimentation du Tableau de bord,des pages equipements, stations, équipements, incidents et historiques
- Plus grande difficulté et comment elle a été surmontée : à compléter
- Fonctionnalités non réalisées et pourquoi : à compléter
- Trois points à retenir du cours et du projet : à compléter
