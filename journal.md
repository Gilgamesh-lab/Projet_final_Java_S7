# Journal -- Supervision des équipements et suivi des incidents (ALTN72, 2026-27)

**Groupe :** Christophe HUANG, Fabrice AMEGADJEN

## README
- Prérequis : Java 25, Maven (le wrapper `mvnw` est fourni), MariaDB ou MySQL.
- Initialiser la base : créer une base vide `Base_ALTN72`. Au démarrage, Spring lance `schema.sql` (les tables) puis
  `data.sql` (les données de démo). La base est donc recréée à chaque lancement.
- Configuration : aucun mot de passe n'est écrit dans le dépôt, tout passe par des variables d'environnement :

  | Variable | Rôle | Valeur par défaut |
  |---|---|---|
  | `DB_URL` | URL de la base | `jdbc:mariadb://localhost:3306/Base_ALTN72` |
  | `DB_USER` | utilisateur | `root` |
  | `DB_PASSWORD` | mot de passe | vide |
  | `DB_INIT_MODE` | `always` pour recréer la base au démarrage, `never` sinon | `always` |

- Lancer : `./mvnw spring-boot:run` puis ouvrir http://localhost:8080
  - avec un mot de passe sous PowerShell : d'abord `$env:DB_PASSWORD="motdepasse"` puis `./mvnw spring-boot:run`
  - dans IntelliJ : Run > Edit Configurations > Environment variables : `DB_PASSWORD=motdepasse`
  - avec PostgreSQL (tables créées par Hibernate, pas de données de démo) :
    `./mvnw spring-boot:run -Dspring-boot.run.profiles=fabrice`, avec `DB_PASSWORD` défini.
- Tests : `./mvnw verify` (pas besoin de base de données).
- Documentation de l'API : `/swagger-ui-custom.html`, une fois connecté.
- Application déployée : https://projetfinaljavas7-production-a95e.up.railway.app (Railway, avec le `Dockerfile` du
  dépôt et une base MySQL 8).
- Comptes de test :
  - Responsable d'exploitation : `responsable` / `responsable123`
  - Agent : `agent` / `agent123`
- Données de démo (les dates dépendent de `NOW()`, elles restent donc cohérentes) :
  - 4 stations, 8 équipements, 3 prestataires.
  - des incidents non affectés ouverts depuis 5 h, 47 h, 48 h, 71 h et 72 h pour voir le vert, l'orange et le rouge.
  - 2 incidents en cours et 10 incidents résolus, dont 2 évalués.
  - `ASC-001` et `ASC-002` sont des équipements fragiles.
- Limites connues :
  - `schema.sql` est écrit pour MariaDB, il ne marche pas tel quel sous PostgreSQL.
  - les données ajoutées pendant une démo sont perdues au redémarrage et à chaque redéploiement en ligne.
  - certains réseaux bloquent les adresses de Railway. Si l'URL ne répond pas, il faut essayer un autre réseau (partage
    de connexion par exemple) ou lancer l'application en local.

## ARCHITECTURE
- Dépôt : https://github.com/Gilgamesh-lab/Projet_final_Java_S7
- Organisation du code :
  - `modele` : les entités et les enums.
  - `repository` : les accès à la base (requêtes dérivées, JPQL et une requête native).
  - `service` : les règles métier.
  - `exception` : nos exceptions.
  - `controleur` : les contrôleurs, avec les vues Thymeleaf dans `templates`.
  - `securite` : la connexion et les droits par profil.
- Schéma :

```
Navigateur
   │  requête HTTP
   ▼
ConnexionInterceptor ── non connecté ──► /login
   │  profil interdit ──► 403
   ▼
Contrôleurs ──► vues Thymeleaf
   │
   ▼
Services (règles métier)
   │
   ▼
Repositories
   │
   ▼
MariaDB en local, MySQL 8 en ligne
```

## TESTS
- Stratégie : on teste les règles métier avec JUnit et Mockito, sans lancer Spring ni la base. Les tests restent donc
  rapides. Pour les règles qui dépendent de l'heure, on utilise un `Clock` fixe : ça permet de tester 47 h 59 ou 48 h
  pile sans attendre.
- Commande : `./mvnw verify`
- Couvert (66 tests) :
  - `IndicateurServiceTest` (8) : vert à 47 h et 47 h 59, orange à 48 h et 71 h 59, rouge à 72 h, « pris en charge »
    pour un incident affecté, pas d'indicateur pour un incident résolu.
  - `TableauDeBordServiceTest` (7) : le tri, les filtres par station et par niveau, les compteurs.
  - `EvaluationServiceTest` (7) : évaluation correcte, refus si l'incident n'est pas résolu, n'a pas de prestataire ou
    est déjà évalué, refus des notes 0, 6 et vide.
  - `StatistiquesServiceTest` (4) : les données envoyées aux graphiques.
  - `IncidentTest` (19) : déclaration (F4), affectation (F7), résolution (F8), modification (F6) et règles de l'API.
  - `EquipementTest` (6) et `StationTest` (7) : lecture, ajout, modification, suppression et refus de supprimer ce qui
    a des incidents.
  - `FragiliteServiceTest` (8) : les cas limites de l'équipement fragile.
- Non couvert :
  - les contrôleurs, les pages et l'API : on les a testés à la main, on n'a pas fait de tests MockMvc.
  - le démarrage de l'application : il faudrait une base disponible, alors que `./mvnw verify` doit marcher sans base.
    On s'en est rendu compte quand une requête invalide empêchait l'application de démarrer alors que les tests
    passaient tous.
  - les requêtes JPQL et native : on les a vérifiées en lançant l'application sur MariaDB avec `data.sql`.

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

- Ce qui est modifiable (F6) : seulement la description d'un incident non résolu, par les deux profils, car l'agent
  doit pouvoir préciser la panne qu'il a signalée. Le statut et le prestataire passent par F7 et F8. Un incident résolu
  ne se modifie plus, il sert de trace.
- API REST (F12) : le `PUT` permet de changer la description ou de passer à `RESOLU`, avec les mêmes règles que les
  pages. Il renvoie 400 si la saisie est mauvaise, 403 si le profil n'a pas le droit, 404 si l'incident n'existe pas et
  409 s'il est déjà résolu.
- Connexion (F1) :
  - on utilise une session et un intercepteur (`ConnexionInterceptor`) plutôt que tout Spring Security : pour deux
    profils ça suffit et c'est plus simple à expliquer.
  - les mots de passe sont hachés en BCrypt, on ne prend que `spring-security-crypto` pour ça.
  - en session on garde juste l'id, le prénom et le profil, jamais le mot de passe.
  - les droits sont vérifiés côté serveur avec l'annotation `@ReserveAuProfil` : même en tapant l'URL à la main, un
    profil non autorisé reçoit une 403.
- Base de données :
  - les valeurs des `ENUM` SQL ont les mêmes noms que les enums Java, ça évite un convertisseur.
  - une seule évaluation par incident avec `UNIQUE` et note de 1 à 5 avec `CHECK`.
  - l'indicateur d'urgence n'est pas stocké, il est calculé à chaque affichage.
- Indicateur d'urgence (RG3 à RG5) :
  - le calcul est dans `IndicateurService`, avec les seuils dans `SEUIL_ORANGE_HEURES` et `SEUIL_ROUGE_HEURES`.
  - on compare avec `Duration` et pas en heures entières, pour que 47 h 59 reste bien vert.
  - l'heure vient d'un `Clock` injecté (`HorlogeConfig`), ce qui permet de la fixer dans les tests.
- Tableau de bord (F11) : les incidents en attente passent d'abord, du plus ancien au plus récent (donc les rouges en
  haut), puis ceux pris en charge. On peut filtrer par station et par niveau.
- Exceptions : on a gardé `ResponseStatusException` plutôt qu'un `@ControllerAdvice`, avec trois exceptions à nous :
  - `RessourceIntrouvableException` (404) hérite de `ResponseStatusException`, Spring affiche directement notre page
    d'erreur.
  - `RegleMetierException` : une opération interdite, comme réaffecter un incident résolu.
  - `SaisieInvalideException` : une donnée manquante ou incorrecte.
  - Le message n'est pas affiché pareil partout (formulaire, liste, réponse de l'API), donc chaque contrôleur attrape
    l'exception. Un `@ControllerAdvice` nous aurait peu aidés ici.
- SOLID :
  - Responsabilité unique (S), appliqué : `IndicateurService` calcule l'indicateur, `FragiliteService` s'occupe de la
    fragilité, `ConnexionInterceptor` protège les pages. On ne répète pas ça dans chaque contrôleur.
  - Inversion des dépendances (D), appliqué : les services reçoivent `Clock`, `PasswordEncoder` et les repositories par
    injection. C'est ce qui nous permet de les tester avec une horloge fixe et des mocks.
  - Ouvert / fermé (O), pas recherché : les seuils ne changent pas, des constantes suffisent. Créer des classes pour
    pouvoir les étendre aurait compliqué le code pour rien.
  - Liskov (L) : on n'a presque pas d'héritage, seulement nos exceptions, qui restent utilisables comme leur classe mère.
  - Ségrégation des interfaces (I) : on n'a pas eu besoin de créer nos propres interfaces, à part la petite projection
    `NombreParStation`.
- Requête SQL native : `IncidentRepository.compterActifsParStation()` compte les incidents non résolus par station pour
  la page Statistiques.
  - Avantage : c'est un simple `GROUP BY` sur trois tables, calculé par la base sans charger tous les incidents.
  - Inconvénients : elle n'est pas vérifiée au démarrage et elle dépend des noms de tables. On a eu le cas avec
    `FROM Station` : ça marchait sous Windows mais pas sous Linux, où MariaDB distingue les majuscules.
  - La même chose en JPQL aurait été vérifiée au démarrage. On l'a laissée en native car le sujet en demande une.
- @Transactional :
  - `EvaluationService.evaluer` : on vérifie qu'il n'y a pas déjà une évaluation puis on l'ajoute, en une seule opération.
  - `IncidentService` (affectation, résolution, modification) et les suppressions : la lecture et l'écriture vont ensemble.
  - `supprimerStation` supprime aussi les équipements de la station et `importerJson` enregistre plusieurs stations :
    soit tout passe, soit rien (RG10).
  - pas de transaction sur les simples lectures, ça n'apporte rien.

## LOT ALPHA
### Fonctionnalité transverse 1 : visualisation des données (piste 2)
- Besoin : le tableau de bord montre les incidents un par un. Le responsable a aussi besoin d'une vue d'ensemble.
- Périmètre : une page `/statistiques`, réservée au responsable, avec trois graphiques : incidents actifs par station,
  incidents actifs par niveau et note moyenne des prestataires.
- Choix et essais :
  - Chart.js chargé depuis un CDN, car c'est simple à mettre en place.
  - `th:inline="javascript"` pour passer les listes Java à la page.
  - les calculs sont faits par la base (requête native et JPQL).
  - on reprend `getCompteurs()` du tableau de bord pour avoir les mêmes chiffres des deux côtés.
- Ce qui fonctionne : les trois graphiques, un message quand il n'y a pas de données, la 403 pour l'agent, 4 tests.
- Difficultés : le `FROM Station` en majuscule, vu plus haut, qu'on a corrigé avant le déploiement.
- Reste à faire : l'évolution dans le temps. Les prestataires sans évaluation n'apparaissent pas non plus dans le
  troisième graphique.

### Fonctionnalité transverse 2 : import / export JSON (piste 9)
- Besoin : ajouter plusieurs stations d'un coup et récupérer l'historique dans un fichier.
- Périmètre : import de stations depuis la page des stations (responsable uniquement) et export de l'historique en
  `incidents.json`.
- Choix et essais :
  - JSON plutôt que CSV : Jackson est déjà dans le projet, il n'y a rien à ajouter et on garde les données liées à
    l'incident (équipement, station, prestataire, évaluation).
  - l'import est dans une transaction : si une station est refusée, aucune n'est ajoutée.
  - on ignore l'id présent dans le fichier pour ne pas écraser une station existante.
- Ce qui fonctionne : l'import, avec le nombre de stations ajoutées et un message si le fichier est vide ou invalide,
  s'il manque un champ ou si l'identifiant existe déjà. L'export avec les évaluations.
- Difficultés :
  - `Incident` et `Evaluation` se référencent l'un l'autre, ce qui faisait une boucle infinie dans le JSON. On l'a
    réglé avec `@JsonIgnore`.
  - avec Jackson 3, un JSON invalide lève une `JacksonException` et plus une `IOException`, ça donnait une erreur 500.
- Reste à faire : l'import d'équipements et l'export en CSV.

## Évolution séance finale
1. Description : un équipement est fragile s'il a eu au moins 3 incidents ouverts sur les 30 derniers jours, quel que
   soit leur statut.
   - `service/FragiliteService.java` : la règle, avec le seuil et la fenêtre dans `SEUIL_INCIDENTS` et `FENETRE_JOURS`.
   - `controleur/EquipementControleur.java` et `detailsEquipement.html` : le badge sur la fiche.
   - `controleur/IncidentControleur.java` et `tableauDeBord.html` : le badge sur le tableau de bord.
   - `controleur/IncidentControleur.java` et `pageAccueil.html` : le message de confirmation après une déclaration,
     avec le mot « fragile » si besoin.
   - `fragments/entete.html` : le badge, écrit à un seul endroit.
   - `data.sql` : des incidents en plus pour voir les différents cas.
2. Choix de conception :
   - rien n'est enregistré en base, la fragilité est recalculée à chaque consultation (EF4).
   - on utilise `Clock` comme pour l'indicateur d'urgence, ce qui permet de tester les limites de la fenêtre.
   - raccourci : le calcul se fait en Java à partir de tous les incidents et pas avec une requête `COUNT`. On peut
     ainsi tester les cas limites sans base et la règle n'existe qu'à un seul endroit. Par contre, ce n'est pas le
     plus rapide si le nombre d'incidents devient très grand.
3. Tests ajoutés (`FragiliteServiceTest`, 8 tests) : 2 incidents non fragile, 3 incidents fragile, 3 incidents dont
   1 résolu, un incident à 30 j et 1 min non compté, un incident à 29 j 23 h 59 compté, un incident à 30 j pile compté,
   2 incidents récents et 5 de 45 jours non fragile, équipement voisin non touché.
   - Limites connues : les badges et le message ne sont pas couverts par des tests automatiques, on les a vérifiés à
     la main. Les données de démo ne contiennent pas d'incident à la limite exacte des 30 jours, car la fenêtre est
     glissante et le résultat changerait quelques minutes après le démarrage.

## BILAN
- Contribution de chacun :
  - Christophe HUANG : base de données (`schema.sql`, `data.sql`), maquettes et refonte du front, connexion et droits
    par profil (F1), indicateur d'urgence, service du tableau de bord (F11), évaluation des prestataires (F10),
    statistiques (lot alpha), pages d'erreur, tests, déploiement, corrections et journal.
  - Fabrice AMEGADJEN : entités JPA, stations, liaison des pages équipements, incidents, historique et tableau de bord,
    déclaration d'un incident (F4), affectation d'un prestataire (F7), résolution (F8), API REST et Swagger (F12),
    import / export JSON (lot alpha), tests des services, corrections, début de l'évolution équipement fragile.
- Plus grande difficulté : travailler sur deux bases différentes, MariaDB pour l'un et PostgreSQL pour l'autre. Sous
  PostgreSQL les tables sont créées par Hibernate, donc une modification pouvait marcher d'un côté et casser `schema.sql`
  ou `data.sql` de l'autre. Par exemple, une erreur dans `data.sql` a empêché l'application de démarrer en ligne. On a
  fini par prendre MariaDB comme référence et par relancer l'application avec `data.sql` avant de pousser.
- Autres problèmes rencontrés :
  - des marqueurs de conflit Git sont arrivés sur `master` dans une page après une fusion.
  - pour Railway, il a fallu autoriser le dépôt GitHub et certains réseaux bloquaient le site.
  - dans IntelliJ, la connexion plantait car le nom des `@RequestParam` n'était pas retrouvé. On a écrit les noms en
    entier dans les annotations.
- Fonctionnalités non réalisées :
  - F1 à F12 et les deux pistes du lot alpha sont faites.
  - il n'y a pas d'écran pour ajouter ou modifier un équipement : ce n'est pas demandé et on a préféré retirer les
    routes pas finies plutôt que de les laisser en erreur.
  - pas de tests de contrôleurs ni de repositories, par manque de temps.
- Trois points à retenir :
  - séparer en couches permet de tester les règles métier sans base ni navigateur.
  - les droits se vérifient côté serveur, masquer un bouton ne protège rien.
  - il faut tester tôt avec de vraies données et dans un environnement proche du déploiement, car certaines erreurs ne
    se voient pas en local, ni avec les tests unitaires.
