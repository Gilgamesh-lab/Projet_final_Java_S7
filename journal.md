# Journal -- Supervision des équipements et suivi des incidents (ALTN72, 2026-27)

**Groupe :** Christophe HUANG, Fabrice AMEGADJEN

## README
- Prérequis : Java 25, Maven (wrapper `mvnw` fourni), MariaDB (ou MySQL).
- Initialiser la base : créer une base vide `Base_ALTN72`. Au démarrage, Spring exécute
  `src/main/resources/schema.sql` (tables) puis `src/main/resources/data.sql` (données de démonstration) :
  la base est recréée à chaque lancement.
- Configuration : aucun mot de passe n'est écrit dans le dépôt. La connexion se règle par variables
  d'environnement, avec des valeurs par défaut pour un MariaDB local :

  | Variable | Rôle | Valeur par défaut |
  |---|---|---|
  | `DB_URL` | URL JDBC | `jdbc:mariadb://localhost:3306/Base_ALTN72` |
  | `DB_USER` | utilisateur | `root` |
  | `DB_PASSWORD` | mot de passe | vide |
  | `DB_INIT_MODE` | `always` pour recréer la base au démarrage, `never` sinon | `always` |

- Lancer : `./mvnw spring-boot:run` puis ouvrir http://localhost:8080
  - avec un mot de passe, sous PowerShell : `$env:DB_PASSWORD="motdepasse"; ./mvnw spring-boot:run`
    (dans IntelliJ : Run > Edit Configurations > Environment variables : `DB_PASSWORD=motdepasse`) ;
  - variante PostgreSQL (tables créées par Hibernate, sans données de démonstration) :
    `./mvnw spring-boot:run -Dspring-boot.run.profiles=fabrice`.
- Tests : `./mvnw verify` (tests unitaires, sans base de données).
- Documentation de l'API REST : `/swagger-ui-custom.html`, après connexion.
- Application déployée : https://projetfinaljavas7-production-a95e.up.railway.app
  (Railway, à partir du `Dockerfile` du dépôt, avec une base MySQL 8 configurée par `DB_URL`, `DB_USER` et `DB_PASSWORD`).
- Comptes de test :
  - Responsable d'exploitation : `responsable` / `responsable123`
  - Agent : `agent` / `agent123`
- Données de démonstration (dates relatives à `NOW()`, donc toujours cohérentes avec l'heure courante) :
  - 4 stations, 8 équipements, 3 prestataires ;
  - incidents non affectés ouverts depuis 5 h, 47 h, 48 h, 71 h et 72 h (limites vert / orange / rouge) ;
  - 2 incidents en cours affectés à un prestataire, 3 incidents résolus dont 2 évalués et 1 à évaluer (F10).
- Limites connues :
  - `schema.sql` est écrit pour MariaDB (`AUTO_INCREMENT`, `ENUM`, `UNSIGNED`) : il ne s'exécute pas tel quel sous PostgreSQL ;
  - la base est recréée à chaque démarrage : les données saisies pendant une démonstration sont perdues au redémarrage,
    y compris sur la version déployée à chaque redéploiement ;
  - Railway a signalé un incident au moment du déploiement : certains réseaux bloquent ses adresses. Si l'URL ne répond
    pas, essayer un autre réseau (partage de connexion par exemple) ou lancer l'application en local.

## ARCHITECTURE
- Dépôt : https://github.com/Gilgamesh-lab/Projet_final_Java_S7
- Organisation en couches :
  - `modele` : entités JPA et énumérations ;
  - `repository` : repositories Spring Data JPA (requêtes dérivées, JPQL et native) ;
  - `service` : règles métier ;
  - `exception` : exceptions de l'application ;
  - `controleur` : contrôleurs Spring MVC, vues Thymeleaf dans `src/main/resources/templates` ;
  - `securite` : connexion, protection des pages et des droits par profil.
- Schéma :

```
Navigateur
   │  requête HTTP
   ▼
ConnexionInterceptor ── non connecté ──► /login
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
MariaDB en local, MySQL 8 en ligne (schema.sql + data.sql)
```

## TESTS
- Stratégie : les règles métier sont testées en tests unitaires JUnit, sans démarrer Spring ni la base, donc rapides
  et indépendants des données. L'heure est fixée grâce à une horloge (`Clock`) injectée dans les services, ce qui
  permet de tester précisément les seuils sans dépendre de l'heure réelle. Les repositories sont remplacés par des
  objets simulés (Mockito) : on teste la logique du service, pas la base.
- Commande : `./mvnw verify`
- Couvert (58 tests) :
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
  - statistiques (`StatistiquesServiceTest`, 4 tests) : ordre des stations, quatre indicateurs dans l'ordre, arrondi de
    la note moyenne, graphique vide sans évaluation.
  - services des équipements et stations (`EquipementTest` 6 tests, `StationTest` 7 tests) : lecture, ajout,
    modification, suppression, refus de supprimer ce qui a des incidents, refus d'une station sans nom.
  - service des incidents (`IncidentTest`, 19 tests) : lecture, ajout, suppression, déclaration (F4) avec refus d'une
    description vide, d'un équipement manquant et d'un identifiant existant, affectation (F7) avec refus d'un incident
    résolu ou inconnu, résolution (F8), modification de la description (F6), règles de l'API (F12).
- Non couvert :
  - les contrôleurs, les pages et l'API REST (pas de tests MockMvc) : vérifiés à la main ;
  - le démarrage complet de l'application : un test `@SpringBootTest` aurait besoin d'une base disponible, alors que
    `./mvnw verify` doit fonctionner sans base ;
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
| F12    | API REST                         | Accès complet              | Lecture seule                         |
| -      | Stations (ajout, modification, suppression, import) | Accès complet | Lecture seule               |
| -      | Supprimer un équipement ou un incident | Accès complet        | Interdit                              |

- Modifications autorisées (F6) : seule la description d'un incident non résolu est modifiable, par les deux profils.
  L'agent signale les pannes et peut donc préciser sa description. Le statut et le prestataire ne changent que par F7
  et F8, réservés au responsable. Un incident résolu n'est plus modifiable : il sert de trace dans l'historique.
- API REST (F12) : le `PUT` ne permet que de modifier la description ou de passer à `RESOLU`, pour respecter les mêmes
  règles que les pages. Il répond 400 si la saisie est invalide, 403 si le profil n'est pas autorisé, 404 si l'incident
  n'existe pas et 409 s'il est déjà résolu.

- Connexion (F1) et sécurité :
  - session HTTP simple plutôt que Spring Security complet : un intercepteur (`ConnexionInterceptor`) suffit pour
    deux profils et reste facile à expliquer ;
  - seul `spring-security-crypto` est utilisé, pour le hachage BCrypt des mots de passe (jamais stockés en clair, §7.7) ;
  - en session on garde un objet `UtilisateurConnecte` (id, prénom, profil), jamais le mot de passe haché ;
  - l'identifiant de session est régénéré après la connexion (fixation de session) ;
  - un utilisateur non connecté est redirigé vers `/login`, y compris pour l'API (`/incidents/api`) ;
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
- Exceptions : trois exceptions personnalisées, avec des messages en français, plutôt qu'un `@ControllerAdvice`.
  - `RessourceIntrouvableException` (404) hérite de `ResponseStatusException` : Spring affiche directement notre page
    d'erreur, sans code supplémentaire.
  - `RegleMetierException` : opération interdite par une règle (incident résolu réaffecté, station avec des incidents
    supprimée...). `SaisieInvalideException` : donnée manquante ou incorrecte.
  - Le contrôleur attrape ces deux dernières pour afficher le message sur la page, ou pour choisir le code HTTP dans
    l'API : la réponse n'est pas la même partout, un traitement global aurait donc peu simplifié le code.
  - Spring Boot affiche automatiquement nos pages `templates/error/404.html` et `error/403.html`, avec le bandeau et
    le message du contrôleur (`spring.web.error.include-message=always`).
- SOLID :
  - Responsabilité unique (S) : `IndicateurService` ne fait que calculer l'indicateur, `ConnexionService` que vérifier
    les identifiants ; la protection des pages est isolée dans `ConnexionInterceptor` au lieu d'être répétée dans
    chaque contrôleur.
  - Inversion des dépendances (D) : les services dépendent d'abstractions injectées par Spring (`Clock`,
    `PasswordEncoder`, interfaces des repositories) et non d'implémentations concrètes. C'est ce qui permet de
    tester `IndicateurService` avec une horloge fixe, et on pourrait changer d'algorithme de hachage sans toucher
    à `ConnexionService`.
- Requête SQL native : `IncidentRepository.compterActifsParStation()`, nombre d'incidents non résolus par station,
  affiché sur la page Statistiques. Le résultat est lu à travers une interface de projection (`NombreParStation`) plutôt
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
### Fonctionnalité transverse 1 : visualisation des données (piste 2)
- Besoin : donner au responsable d'exploitation une vue d'ensemble que le tableau de bord, ligne par ligne, ne montre
  pas : où se concentrent les incidents, leur niveau d'attention et la qualité des prestataires.
- Périmètre : une page `/statistiques`, réservée au responsable, avec trois graphiques : incidents actifs par station,
  incidents actifs par niveau d'attention, note moyenne des prestataires.
- Choix et essais :
  - Chart.js, chargé depuis un CDN : simple, sans étape de construction côté front ;
  - `th:inline="javascript"` : Thymeleaf écrit les listes Java directement dans la page, sans appel supplémentaire ;
  - agrégations faites par la base (requête native par station, JPQL `AVG` / `COUNT` par prestataire) plutôt qu'en Java ;
  - réutilisation de `TableauDeBordService.getCompteurs()` pour que les chiffres restent cohérents avec le tableau de bord ;
  - une page à part, pour ne pas fragiliser le tableau de bord du socle.
- Ce qui fonctionne : les trois graphiques, les messages quand il n'y a pas de données, la restriction au responsable
  (403 pour l'agent), 4 tests unitaires sur la préparation des données.
- Difficultés : la requête native écrite avec des majuscules (`FROM Station`) fonctionnait sous Windows mais n'aurait
  pas fonctionné sous Linux ; nous l'avons remise en minuscules avant le déploiement.
- Reste à faire : l'évolution dans le temps (incidents par semaine, délais de résolution) ; les prestataires sans
  évaluation n'apparaissent pas dans le troisième graphique.
### Fonctionnalité transverse 2 : import / export JSON (piste 9)
- Besoin : ajouter plusieurs stations d'un coup sans passer par le formulaire, et récupérer l'historique des incidents
  résolus dans un fichier réutilisable par un autre outil.
- Périmètre : import de stations depuis un fichier JSON, réservé au responsable, depuis la page des stations ; export
  de l'historique en `incidents.json`, depuis la page de l'historique.
- Choix et essais :
  - JSON plutôt que CSV : Jackson est déjà présent dans le projet, aucune dépendance à ajouter, et les données liées
    (équipement, station, prestataire, évaluation) gardent leur structure ;
  - import dans une transaction : si une station est refusée, aucune n'est enregistrée (RG10) ;
  - l'identifiant technique éventuellement présent dans le fichier est ignoré, pour ne jamais écraser une station.
- Ce qui fonctionne : import avec message du nombre de stations ajoutées ; messages clairs pour un fichier vide, un JSON
  invalide, un champ manquant ou un identifiant déjà utilisé ; export avec les évaluations.
- Difficultés : la relation `Incident` / `Evaluation` provoquait une boucle infinie lors de l'écriture du JSON, réglée
  avec `@JsonIgnore` ; avec Jackson 3, un JSON invalide ne lève plus d'`IOException` mais une `JacksonException`, qu'il
  a fallu attraper pour ne pas afficher une erreur 500.
- Reste à faire : import d'équipements et export au format CSV.

## BILAN
- Contribution de chacun :
  - Christophe HUANG : base de données (`schema.sql`, `data.sql`), alignement des entités sur le schéma, maquettes
    des pages, connexion / déconnexion et droits par profil (F1), calcul de l'indicateur d'urgence, service et
    requêtes du tableau de bord (F11), évaluation des prestataires (F10), statistiques (lot alpha), pages d'erreur,
    tests unitaires, configuration et déploiement, journal.md ;
  - Fabrice AMEGADJEN : entités JPA, stations (`StationRepository`, `StationService`, `StationControleur`),
    configuration PostgreSQL, alimentation des pages stations, équipements, incidents, historique et tableau de bord,
    signaler un incident (F4), affecter ou réaffecter un prestataire (F7), déclarer un incident résolu (F8),
    API REST (F12), tests des services.
- Plus grande difficulté et comment elle a été surmontée : travailler chacun sur une base différente (MariaDB et
  PostgreSQL). Sous PostgreSQL, les tables étaient créées par Hibernate à partir du code ; une modification pouvait donc
  fonctionner d'un côté et casser `schema.sql` ou `data.sql` de l'autre, comme le renommage d'une valeur de `Profil`,
  qui bloquait la connexion du responsable. Nous avons retenu MariaDB comme référence, et relancé l'application avec
  les données de démonstration pour vérifier les changements.
- Fonctionnalités non réalisées et pourquoi :
  - les fonctionnalités F1 à F12 et les deux pistes du lot alpha sont réalisées ;
  - pas d'écran pour ajouter ou modifier un équipement : ce n'est pas demandé par le socle, et nous avons préféré retirer
    les routes inachevées plutôt que de les laisser en erreur. La suppression reste possible pour le responsable ;
  - pas de tests de contrôleurs ni de repositories (voir TESTS), par manque de temps : ils ont été vérifiés à la main.
- Trois points à retenir du cours et du projet :
  - la séparation en couches : les règles métier dans les services sont testables sans base ni navigateur ;
  - les règles et les droits s'appliquent côté serveur : masquer un bouton ne protège rien ;
  - tester tôt avec de vraies données et un environnement proche du déploiement révèle des erreurs invisibles en
    local, comme la casse des noms de tables.
