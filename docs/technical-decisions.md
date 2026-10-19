# Décisions techniques

## 1. Pivot backend FastAPI → Spring Boot 4

Le backend a d'abord été développé en Python/FastAPI (stack par défaut du
cahier des charges), fonctionnel et testé, puis **entièrement réécrit en
Spring Boot 4 / Java 17** à la demande explicite de l'utilisateur en cours
de projet. Le contrat d'API REST (routes, noms de champs JSON) a été
volontairement préservé à l'identique pour que le frontend React déjà
développé continue de fonctionner sans modification de logique — seul un
bug frontend préexistant (voir §4) a été corrigé au passage.

Spring Boot 4 est une version très récente (Spring Framework 7) qui a
introduit plusieurs changements structurels rencontrés et documentés ici :

- **Jackson 3 par défaut** : le bean `ObjectMapper` auto-configuré par Spring
  Boot est désormais `tools.jackson.databind.ObjectMapper` (Jackson 3), et
  non plus `com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2). Les
  utilitaires internes du projet qui ont besoin d'un ObjectMapper classique
  (`JsonUtil`, `DemoProvider`, `SecurityConfig`) instancient donc leur propre
  `ObjectMapper` Jackson 2 plutôt que d'injecter le bean Spring — voir le
  commentaire dans `JsonUtil.java`.
- **`spring-boot-starter-webmvc` vs `spring-boot-starter-web`** : le premier
  (proposé par Spring Initializr par défaut) n'inclut pas l'auto-config JSON ;
  `spring-boot-starter-web` (qui inclut `spring-boot-starter-jackson`) est le
  bon choix pour une API REST classique.
- **`TestRestTemplate` déplacé** dans un module dédié
  `spring-boot-resttestclient`, lui-même dépendant de `spring-boot-restclient`
  pour `RestTemplateBuilder`, et nécessitant l'annotation explicite
  `@AutoConfigureTestRestTemplate` (elle était implicite en Spring Boot 3).
- **`Specification.where(null)`** (Spring Data JPA) lève désormais une
  exception au lieu d'agir comme point de départ neutre — la construction
  dynamique de filtres combine désormais les spécifications à partir d'une
  référence `null` explicite plutôt que via `Specification.where(null)`.
- **`HttpStatus.UNPROCESSABLE_ENTITY`** a été renommé `UNPROCESSABLE_CONTENT`
  (alignement sur l'intitulé HTTP à jour, "422 Unprocessable Content").

## 2. Choix des sources de données automobiles

Voir `api-comparison.md` pour l'étude complète. Décision retenue :

- **NHTSA vPIC** : seule API réellement gratuite, publique et sans clé parmi
  celles étudiées → utilisée pour le décodage VIN réel et les listes
  marque/modèle.
- **Aucune API gratuite** ne fournit de spécifications techniques complètes
  (puissance, couple, consommation, CO₂...) sans compte payant ou contrat
  partenaire → un jeu de données local curé (27 véhicules réels, valeurs
  approximatives à but pédagogique) comble ce manque, **toujours étiqueté
  "Demo data"**, jamais présenté comme une réponse d'API en direct.
- Recherche par plaque d'immatriculation : aucune API légale et gratuite
  trouvée (contraintes légales fortes, ex. DPPA aux États-Unis) → le
  formulaire existe mais indique explicitement l'indisponibilité plutôt que
  de simuler un résultat.

## 3. PostgreSQL sans droits administrateur

L'environnement de développement ne disposait ni de droits administrateur
Windows, ni d'un Docker Desktop opérationnel (l'installation présente
échouait au démarrage du backend WSL2, sans possibilité de diagnostic
interactif). Pour respecter malgré tout l'exigence "PostgreSQL réel, local",
un serveur PostgreSQL 16 authentique a été lancé via le paquet Python
`pgserver` (binaires officiels embarqués, aucun droit admin requis).

Deux bugs de ce paquet sur Windows ont été identifiés et corrigés dans
`docker/pg-embedded/run_pg.py` (documentés en tête de fichier) :

1. Port TCP aléatoire à chaque démarrage → un port fixe (5432) est imposé via
   un monkeypatch ciblé, avec repli automatique sur un port libre si 5432 est
   occupé.
2. Absence des données de fuseau horaire IANA dans la distribution Windows du
   paquet, provoquant l'échec de **toute** connexion JDBC/libpq (même avec
   `TimeZone=UTC`) — corrigé en récupérant une fois ce dossier de données
   depuis un artefact Maven public officiel (`zonky/embedded-postgres-binaries`),
   qui contient une distribution PostgreSQL complète pour Windows.

Ce mécanisme est documenté comme solution de secours dans
`docs/installation.md` ; l'option recommandée reste Docker Compose ou une
installation PostgreSQL standard quand l'environnement le permet.

## 4. Bugs réels trouvés et corrigés pendant les tests

Le développement a suivi la règle « exécuter réellement, jamais supposer » :
chaque fonctionnalité a été testée en conditions réelles (navigateur,
`curl`, tests automatisés), ce qui a permis de détecter :

- **Recherche libre multi-mots cassée** : `"Tesla Model 3"` ne trouvait aucun
  résultat car aucune colonne unique ne contient la phrase complète (marque
  et modèle sont des colonnes séparées). Corrigé en tokenisant la requête et
  en exigeant que chaque mot corresponde à au moins un des champs
  marque/modèle/constructeur/finition (ET logique entre les mots, OU logique
  entre les champs pour un mot donné).
- **Bug d'inscription frontend** : le formulaire envoyait `fullName` au lieu
  de `full_name` attendu par l'API, silencieusement accepté par le
  compilateur Vite en mode développement (pas de vérification TypeScript
  stricte en `dev`) — détecté en testant l'inscription dans un vrai
  navigateur, corrigé, et une passe `tsc -b` complète a confirmé l'absence
  d'autres erreurs de type similaires.
- **`@CreationTimestamp` absent de la réponse juste après création** :
  `userRepository.save()` ne déclenche pas immédiatement la génération des
  timestamps Hibernate dans une méthode `@Transactional` (le flush n'a lieu
  qu'à la validation de la transaction, après la construction de la
  réponse) → remplacé par `saveAndFlush()` partout où l'entité vient d'être
  créée et est immédiatement sérialisée dans la réponse HTTP.

## 5. Hors périmètre (perspectives futures)

Conformément aux sections 39-40 du cahier des charges, ces sujets restent
volontairement non développés :

- Diagnostic IA, maintenance prédictive, gestion de flotte
- Estimation de valeur marchande
- Services connectés, OBD-II temps réel
- BI avancée / aide à la décision automatisée
- Cybersécurité avancée (au-delà des standards JWT/BCrypt/CORS déjà en place)
- Toute fonctionnalité de marketplace, achat, vente ou paiement
