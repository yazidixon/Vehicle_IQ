# Étude comparative des API de données automobiles

**Document réalisé dans le cadre du projet académique "vehicle-platform".**
**Date de la recherche : septembre 2026.**

## Objectif

Ce document recense et compare les API et portails publics de données automobiles susceptibles d'être utilisés par le projet, pour deux besoins distincts :

1. **Décodage VIN / identification de véhicule** (marque, modèle, année, type de carrosserie, usine de fabrication, etc.).
2. **Spécifications techniques complètes** (puissance, couple, dimensions, consommation, émissions de CO2, prix marché).

Chaque source a été vérifiée directement (site officiel, documentation, pages de tarification) au moment de la rédaction. Lorsqu'une information (prix exact, quota précis) n'a pas pu être confirmée publiquement, la mention **"non communiqué publiquement"** est utilisée plutôt que d'inventer un chiffre.

## Méthodologie

Pour chaque API listée, la vérification a porté sur trois éléments :

- **Le site officiel et/ou la documentation technique**, consultés directement pour confirmer l'existence, le statut (en ligne/hors service) et le périmètre fonctionnel réel de l'API.
- **La page de tarification officielle**, lorsqu'elle existe, pour relever les paliers gratuits, les prix affichés et les limites de quota publiées.
- **Des sources secondaires convergentes** (forums développeurs, dépôts GitHub de wrappers, pages d'archivage de statut de site) lorsque l'information sur la disponibilité réelle du service n'était pas suffisamment claire depuis la seule documentation officielle — c'est notamment le cas pour CarQuery API, dont l'instabilité chronique est documentée par plusieurs sources indépendantes en plus de la constatation directe faite pour ce document.

Aucune donnée de tarification ou de quota n'a été extrapolée ou estimée : lorsque l'information n'était pas publiée de façon claire (par exemple le tarif exact au-delà des paliers listés pour Auto.dev, ou les tarifs des accords-cadres Mercedes-Benz/Ford), le document l'indique explicitement plutôt que de présenter un chiffre non vérifié.

---

## Tableau comparatif

| API | Auth | Pricing | Free tier | Avantages | Inconvénients | Verdict / Usage |
|---|---|---|---|---|---|---|
| **NHTSA vPIC** | Aucune (pas de clé, accès public) | Gratuit, aucun modèle payant | Oui — accès complet et illimité en usage raisonnable (limitation automatique du trafic non chiffrée publiquement) | Officiel (gouvernement US), gratuit, sans inscription, couvre VIN, marques, modèles, constructeurs, formats JSON/XML/CSV | Couverture US-centrée, ne fournit pas de prix marché ni de specs détaillées (puissance, conso, CO2) ; pas de garantie de SLA | **Retenu et intégré** — décodage VIN et référentiel marques/modèles |
| **CarQuery API** | Aucune (historiquement sans clé) | Gratuit (historique) | Oui (historique) | Simple, gratuite, ancienne référence pour marques/modèles/trims | Base de données figée depuis 2019 (plus de mise à jour), portail de support constaté **suspendu** lors de la vérification (septembre 2026), fiabilité de service non garantie | **Écarté — service jugé non fiable/obsolète**, ne pas utiliser en production |
| **Edmunds API** | Clé API (obtenue via un compte partenaire) | Non communiqué publiquement (accès partenaire uniquement) | Non — inscriptions publiques fermées depuis 2018 | Données riches (fiches véhicules, avis éditoriaux, prix concessionnaires) quand l'accès est obtenu | Programme ouvert fermé aux nouveaux développeurs ; nécessite un accord commercial via un "Account Executive" Edmunds | **Écarté — accès fermé aux nouveaux inscrits**, inutilisable pour un projet académique sans partenariat |
| **Marketcheck API** | Clé API (query param) ou OAuth 2.0 | Abonnement mensuel + coût à l'appel ; palier payant à partir d'environ 299 $/mois + frais de données (tarif Basic) | Oui — palier gratuit limité à 500 appels/mois, 5 req/s, rayon de recherche 100 miles | Très large couverture (annonces, historiques de prix, VIN, plaques via API tierces), palier gratuit sans carte bancaire pour la découverte | Fonctionnalités avancées (analyse de marché, notation) réservées aux paliers payants ; palier gratuit très limité en volume | **Étudié, non intégré** — utilisable en démonstration limitée mais trop restreint/payant pour un usage complet |
| **CarsXE API** | Clé API (en-tête / paramètre), inscription en ligne rapide | Modèle payant : abonnement de base constaté à 99 $/mois + frais additionnels par appel | Essai gratuit de 7 jours (carte bancaire requise pour activer la clé) | Couvre VIN, valeur marché, images, plaques (reconnaissance de plaques), specs détaillées ; documentation claire, mise en route rapide | Pas de palier gratuit permanent, coût mensuel dès la sortie de la période d'essai, nécessite une carte bancaire | **Écarté pour l'intégration finale** — utile pour un test ponctuel de 7 jours mais non viable sans abonnement payant |
| **Auto.dev** | Clé API | Facturation à l'usage ; paliers "Free / Growth / Scale" | Oui — palier gratuit à 1 000 appels/mois, 5 req/s | Couvre VIN, annonces d'occasion avec prix de marché, spécifications, photos, rappels NHTSA ; palier gratuit sans frais fixes | Volume gratuit modeste (1 000 appels/mois), fonctionnalités avancées et volumes illimités réservés aux paliers payants | **Étudié, non intégré** — palier gratuit intéressant pour un prototype mais insuffisant pour couvrir tous les besoins du projet à long terme |
| **API Ninjas — Cars API** | Clé API (en-tête `X-Api-Key`), inscription gratuite | Paliers payants à partir d'environ 9 $/mois (pour usage commercial et quotas étendus) | Oui, mais limité — les endpoints `/v2/carmakes`, `/v2/carmodels`, `/v2/carfacets` sont accessibles gratuitement ; l'endpoint principal `/v2/cars` (recherche complète avec specs : puissance, cylindrée, poids, etc.) n'est pas autorisé en usage commercial sur le palier gratuit | Beaucoup de champs techniques disponibles (puissance en ch/kW, cylindrée, poids, accélération, vitesse max) quand l'accès complet est payant | Palier gratuit restreint aux endpoints de navigation (marques/modèles), pas d'usage commercial gratuit sur les données techniques complètes | **Écarté pour l'usage commercial/production** — endpoints de base utilisables gratuitement pour prototypage, mais les spécifications complètes nécessitent un abonnement |
| **Mercedes-Benz Developer Portal** | Clé API après inscription ; accès aux données réelles de véhicules soumis à un accord-cadre contractuel signé | Modèles variables selon produit ; non communiqué publiquement de façon uniforme (dépend du contrat) | Oui pour le bac à sable (sandbox) — accès immédiat et gratuit à des véhicules simulés ("softcars") pour tester les API sans véhicule réel ; les données de véhicules réels nécessitent la possession d'un véhicule Mercedes-Benz (BYOCAR) et/ou un accord commercial | Portail professionnel bien documenté, sandbox gratuite disponible immédiatement, large gamme d'API (télématique, statut véhicule, charge EV, diagnostics) | Restreint à l'écosystème Mercedes-Benz (OEM) ; données réelles nécessitent soit un véhicule Mercedes-Benz possédé par le développeur, soit un accord-cadre signé ; inutilisable pour couvrir un parc automobile multi-marques | **Écarté — accès OEM restreint**, pertinent uniquement pour des démonstrations liées spécifiquement à des véhicules Mercedes-Benz |
| **Ford Developer Portal** | Compte développeur + `clientId`/`clientSecret` ; accès aux API de production gated par candidature partenaire | Non communiqué publiquement | Non confirmé — l'accès aux API (FordConnect Query API, Ford Pro API) nécessite un compte et, pour les données de production, une validation partenaire | Documentation présente sur le portail, couverture large pour les flottes Ford Pro (statut véhicule, comportement conducteur, rapports de trajet) | Accès gated derrière une candidature partenaire et le consentement du propriétaire du véhicule (OAuth FordPass) ; pas d'auto-inscription publique pour usage libre | **Écarté — accès partenaire restreint**, inutilisable sans validation commerciale par Ford |
| **Lookup plaque d'immatriculation → véhicule** | Variable | Variable | **Aucune API gratuite, sans inscription et fiable identifiée** | — | Les sites grand public trouvés ("VinGurus", "VehicleLookup.org", "ForCar", "Zilocar") sont des services web de consultation destinés aux particuliers, pas des API documentées pour développeurs ; les véritables API de plaques (ex. Marketcheck, CarsXE, VehicleDatabases.com) sont payantes et/ou restreintes par juridiction ; aux États-Unis, l'accès aux données de plaques est en outre encadré par le *Driver's Privacy Protection Act* (DPPA), qui limite l'usage à des motifs légitimes | **Non intégré — aucune solution gratuite et fiable trouvée.** Cette fonctionnalité est explicitement notée comme non réalisable sans un fournisseur de données payant et une conformité juridictionnelle ; elle n'a pas été simulée ni implémentée dans le projet |

---

## Détail par API

### 1. NHTSA Vehicle API (vPIC)

- **Site officiel** : https://vpic.nhtsa.dot.gov/
- **Documentation** : https://vpic.nhtsa.dot.gov/api/
- **Décodeur en ligne** : https://vpic.nhtsa.dot.gov/decoder/

Le *Vehicle Product Information Catalog* (vPIC) est l'API officielle du National Highway Traffic Safety Administration (NHTSA), l'agence fédérale américaine de sécurité routière. Elle permet de décoder un numéro d'identification de véhicule (VIN) — y compris en lot (jusqu'à 50 VIN par requête) — et d'interroger un référentiel de marques, modèles, constructeurs et identifiants mondiaux de constructeur (WMI). L'accès est entièrement public, sans clé API ni inscription, et les réponses sont disponibles en JSON, XML ou CSV. La documentation mentionne un mécanisme automatique de contrôle du trafic pour préserver la performance du service, mais ne publie pas de quota chiffré précis. Des bases de données autonomes (SQL Server, PostgreSQL) sont également proposées en téléchargement pour les usages à très fort volume, permettant d'éviter les limites de l'API en ligne.

**C'est l'API retenue pour l'intégration réelle du projet** : elle est gratuite, fiable (source gouvernementale), sans friction d'inscription, et couvre exactement le besoin de décodage VIN et de référentiel marques/modèles.

### 2. CarQuery API

- **Site officiel** : https://www.carqueryapi.com/
- **Support/annonces** : https://support.carqueryapi.com/

CarQuery est une API communautaire historique (gratuite, sans clé) fournissant des données de marques, modèles et caractéristiques de base (trims). Cependant, plusieurs sources concordantes indiquent que la base de données n'a plus été mise à jour depuis 2019, et des utilisateurs rapportent des indisponibilités récurrentes ("the service may not be available at this time", forums WordPress). Lors de la vérification effectuée pour ce document, le site de support du service (support.carqueryapi.com) retournait un message **"This Account has been suspended"**, confirmant l'instabilité chronique déjà réputée de ce service.

**Verdict : écarté.** Un service dont la disponibilité n'est pas garantie et dont les données ne sont plus tenues à jour depuis plusieurs années ne peut pas servir de base fiable à un projet, même académique.

### 3. Edmunds API

- **Portail** : https://developer.edmunds.com/
- **Centre d'aide** : https://help.edmunds.com/hc/en-us/articles/4414038118679-Edmunds-Developer-API

Edmunds propose historiquement une API riche (fiches techniques, avis éditoriaux, prix concessionnaires). Toutefois, la documentation du portail elle-même contient une FAQ intitulée *"Are there any exceptions to Edmunds' decision to retire the open API program?"*, confirmant que le programme d'API ouvert a été retiré. Les nouvelles inscriptions publiques sont fermées depuis 2018 ; l'accès est désormais réservé aux partenaires stratégiques, concessionnaires et prestataires de services passant par un "Account Executive" Edmunds.

**Verdict : écarté.** Sans possibilité d'inscription publique, cette API n'est pas exploitable pour un projet académique sans démarche commerciale.

### 4. Marketcheck API

- **Site officiel** : https://www.marketcheck.com/
- **Tarification** : https://www.marketcheck.com/apis/pricing/
- **Documentation** : https://docs.marketcheck.com/

Marketcheck propose un ensemble très large d'API automobiles (annonces de véhicules, historique de prix, VIN, plaques via API tierces, données d'équipements lourds/motos/VR). L'authentification se fait par clé API en paramètre de requête ou par OAuth 2.0. Un palier gratuit existe (500 appels/mois, 5 requêtes/seconde, rayon de recherche limité à 100 miles), sans carte bancaire requise pour la découverte. Le palier payant "Basic" démarre à environ 299 $/mois plus des frais de données, et un tarif à l'appel est également documenté (de 0,001 $ à 0,13 $ selon l'API, voire jusqu'à 0,70 $ pour certaines API tierces).

**Verdict : étudié mais non intégré.** Le palier gratuit est utilisable pour une démonstration limitée, mais trop restreint en volume pour couvrir les besoins réels du projet ; les paliers complets sont payants.

### 5. CarsXE API

- **Site officiel** : https://carsxe.com/
- **Documentation** : https://docs.carsxe.com/docs
- **Blog produit** : https://api.carsxe.com/

CarsXE couvre le décodage VIN, la valeur marché, les images de véhicules, la reconnaissance de plaques d'immatriculation, le décodage de codes défaut OBD, et les rappels de sécurité. L'inscription est rapide (clé API générée immédiatement) et un essai gratuit de 7 jours est proposé. L'activation de la clé nécessite l'ajout d'un moyen de paiement, et le modèle tarifaire constaté est un abonnement mensuel (de l'ordre de 99 $/mois) plus des frais additionnels par appel — aucun palier gratuit permanent n'a été identifié.

**Mise à jour — intégrée en cours de projet.** L'utilisateur a fourni une clé d'essai personnelle. Le schéma OpenAPI officiel (`https://docs.carsxe.com/api/openapi`) a été récupéré et vérifié directement (authentification par paramètre `key`, endpoints `/images` et `/specs` testés en conditions réelles le 2026-09-13) avant toute implémentation, conformément à la règle du projet de ne jamais fabriquer un contrat d'API. Utilisation concrète, avec la clé d'essai de l'utilisateur :
- `/images` (make/model/année/trim) : récupère une vraie photo du véhicule. Constat en conditions réelles : une requête avec un `trim` interne (ex. "1.6 T-GDI HEV") échoue souvent (404) car ce n'est pas un nom commercial reconnu par l'index CarsXE, d'où une recherche en trois passes qui relâche progressivement le trim puis l'année. Les noms de marque accentués (« Škoda ») échouent aussi tant qu'ils ne sont pas translittérés en ASCII (« Skoda ») — un cas réel, pas une hypothèse. Résultat : 27/27 véhicules du catalogue démo illustrés avec une vraie photo après ce correctif.
- `/specs` (VIN) : enrichissement optionnel (équipements, coloris, garanties) implémenté dans l'adaptateur mais non branché sur le flux VIN principal en V1, pour ne pas complexifier le contrat de réponse déjà satisfait par NHTSA.

Chaque image est récupérée **une seule fois par véhicule** puis persistée en base (`vehicles.image_url`) et tracée dans `vehicle_source_data` (provider = `carsxe`) — aucune requête répétée au-delà du premier affichage, ce qui préserve le quota de la clé d'essai. Sans clé (`CARSXE_API_KEY` vide), l'application reste pleinement fonctionnelle : les véhicules s'affichent avec le pictogramme de remplacement, comme prévu section 32 du cahier des charges.

### 6. Auto.dev

- **Site officiel** : https://www.auto.dev/
- **Tarification** : https://www.auto.dev/pricing
- **Documentation** : https://docs.auto.dev/v2

Auto.dev est une plateforme plus récente ciblant aussi bien les développeurs que les agents IA, avec décodage VIN international, annonces de véhicules d'occasion (prix de marché, données concessionnaires), spécifications, photos et rappels NHTSA. La facturation est à l'usage, avec trois paliers : *Free* (1 000 appels/mois, 5 requêtes/seconde), *Growth* (volume mensuel illimité, 10 requêtes/seconde) et *Scale* (volume illimité, 50 requêtes/seconde). Les tarifs précis à l'appel au-delà du palier gratuit n'ont pas pu être confirmés dans le détail public consulté (non communiqué publiquement au-delà des paliers listés).

**Verdict : étudié mais non intégré.** Le palier gratuit (1 000 appels/mois) est intéressant pour un prototype, mais son volume reste limité et la pérennité gratuite n'est pas garantie pour un usage prolongé.

### 7. API Ninjas — Cars API

- **Site officiel** : https://api-ninjas.com/api/cars
- **Tarification** : https://api-ninjas.com/pricing

L'API Cars d'API Ninjas retourne un grand nombre de champs techniques (puissance en ch et kW, cylindrée, nombre de cylindres, poids à vide, accélération, vitesse maximale, type de carrosserie, dates de début/fin de production, etc.), avec authentification par clé API transmise dans l'en-tête `X-Api-Key`. L'inscription est gratuite, mais l'endpoint principal de recherche complète (`/v2/cars`) est explicitement documenté comme **non autorisé en usage commercial sur le palier gratuit** ; seuls les endpoints de navigation (`/v2/carmakes`, `/v2/carmodels`, `/v2/carfacets`) sont librement utilisables. Les paliers payants démarrent autour de 9 $/mois selon la tarification générale d'API Ninjas.

**Mise à jour — intégrée en cours de projet, sur un endpoint différent de celui initialement documenté.** Tests en conditions réelles avec la clé gratuite de l'utilisateur (2026-09-13) :
- `/v2/cars`, `/v2/carmakes`, `/v2/carmodels` (v2, y compris `/v2/carfacets` malgré la documentation publique qui l'annonçait disponible sur tous les paliers) : renvoient tous `{"error": "This endpoint is only available to premium subscribers."}` avec une clé gratuite — confirmation directe, pas une supposition.
- `/v1/cars` (l'ancien endpoint, toujours actif) : **fonctionne avec la clé gratuite**, avec un jeu de champs plus restreint (`class`, `cylinders`, `displacement`, `drive`, `fuel_type`, `transmission`, `year` — `city_mpg`/`highway_mpg`/`combination_mpg` restent verrouillés premium) et sans le paramètre `limit` (réservé premium également).

Le projet utilise donc `/v1/cars` comme **source de secours de recherche** : quand une requête (marque et/ou texte libre) ne trouve aucun résultat dans le catalogue local, le backend interroge `/v1/cars` en direct, normalise la réponse (jamais de champ inventé pour les données non retournées par le palier gratuit) et persiste le véhicule trouvé comme entrée réelle du catalogue (`is_demo_data=false`, source tracée). Cela couvre des marques/modèles hors des 27 véhicules démo curés, avec des données réellement vérifiées plutôt que de renvoyer une liste vide.

### 8. Mercedes-Benz Developer Portal

- **Site officiel** : https://developer.mercedes-benz.com/
- **Produits connectivité** : https://connectivity.mercedes-benz.com/

Le portail développeur officiel de Mercedes-Benz propose un large catalogue d'API orientées OEM : télématique, configurateur de véhicule, fonctions à distance, données techniques et marketing, statut de charge des véhicules électriques, niveau de carburant, kilométrage, diagnostics, et statuts de portes/vitres/toit ouvrant. L'inscription à un compte développeur donne accès à un environnement de type bac à sable ("softcars", véhicules simulés) permettant de tester gratuitement et immédiatement les API sans posséder de véhicule réel. En revanche, l'accès aux données de **véhicules réels** nécessite soit d'être propriétaire d'un véhicule Mercedes-Benz compatible (modèle "Bring Your Own Car"), soit la signature d'un accord-cadre commercial avec Mercedes-Benz.

**Verdict : écarté pour ce projet.** Il s'agit d'un accès OEM par construction, pertinent uniquement pour des cas d'usage spécifiques aux véhicules Mercedes-Benz connectés, pas pour un référentiel généraliste multi-marques.

### 9. Ford Developer Portal

- **Site officiel** : https://developer.ford.com/
- **Catalogue d'API** : https://developer.ford.com/apis

Le Ford Developer Marketplace héberge notamment la *FordConnect Query API* (données véhicule consommateur avec consentement OAuth via FordPass) et une surface d'API *Ford Pro* pour la gestion de flottes (statut véhicule, historique, efficacité énergétique, comportement de conduite, rapports de trajet, santé du véhicule). L'accès nécessite la création d'un compte développeur puis la génération d'identifiants (`clientId` / `clientSecret`) ; l'accès aux données de production est conditionné à une validation de candidature partenaire par Ford, et, pour les données de véhicules consommateurs, au consentement explicite du propriétaire via OAuth.

**Verdict : écarté pour ce projet.** Comme Mercedes-Benz, il s'agit d'un accès OEM gated par candidature partenaire, non adapté à un projet académique généraliste et multi-marques.

### 10. Lookup plaque d'immatriculation → véhicule (recherche d'une solution gratuite)

Une recherche spécifique a été menée pour identifier une API gratuite et sans inscription permettant de retrouver un véhicule à partir de sa plaque d'immatriculation, à des fins de démonstration. **Aucune solution répondant à ces critères n'a été trouvée.** Les résultats obtenus se répartissent en deux catégories :

- Des **sites web grand public** (VinGurus, VehicleLookup.org, ForCar, Zilocar) qui proposent une recherche gratuite sans inscription, mais qui sont des services de consultation en ligne destinés aux particuliers (pages web avec formulaire), et non des API documentées et stables destinées aux développeurs.
- Des **API commerciales réelles** (Marketcheck, CarsXE, VehicleDatabases.com) qui proposent bien un service de "license plate lookup" pour développeurs, mais toutes payantes et/ou nécessitant une démonstration commerciale préalable.

Il convient également de noter que, aux États-Unis, l'accès aux données rattachées à une plaque d'immatriculation est encadré légalement par le *Driver's Privacy Protection Act* (DPPA), qui restreint l'usage de ces données à des motifs légitimes précis — ce qui explique en partie l'absence de service gratuit et ouvert dans ce domaine.

**Verdict : fonctionnalité non implémentée dans le projet.** Aucune API gratuite, fiable et sans inscription n'a pu être identifiée ; il ne s'agit pas d'une lacune de recherche mais d'une contrainte réelle du marché, documentée ici plutôt que contournée par une solution inventée.

---

## Matrice de couverture fonctionnelle

Ce tableau résume, pour les sources réellement exploitables comme API développeur (les sites de consultation grand public du point 10 sont exclus, n'étant pas des API), quelles catégories de données chaque source couvre. "Oui (payant)" signifie que la donnée existe mais n'est accessible qu'au-delà du palier gratuit ou après souscription.

| API | Décodage VIN | Marques/modèles | Specs techniques (puissance, dimensions...) | Prix / valeur marché | Images | Plaques → véhicule |
|---|---|---|---|---|---|---|
| NHTSA vPIC | Oui (gratuit) | Oui (gratuit) | Non | Non | Non | Non |
| CarQuery API | Non | Oui (historique, non maintenu) | Partiel (trims basiques, obsolètes) | Non | Non | Non |
| Edmunds API | Oui (accès fermé) | Oui (accès fermé) | Oui (accès fermé) | Oui (accès fermé) | Oui (accès fermé) | Non |
| Marketcheck API | Oui (payant) | Oui (gratuit limité) | Partiel (payant) | Oui (payant) | Oui (payant, dès 0,001 $/appel) | Oui (via API tierce, payant) |
| CarsXE API | Oui (essai 7 j puis payant) | Oui (payant) | Oui (payant) | Oui (payant) | Oui (payant) | Oui (payant) |
| Auto.dev | Oui (gratuit limité) | Oui (gratuit limité) | Oui (gratuit limité) | Oui (annonces, gratuit limité) | Oui (payant/limité) | Non |
| API Ninjas Cars API | Non (API séparée "VIN Lookup") | Oui (gratuit) | Oui (payant pour usage commercial) | Non | Non | Non |
| Mercedes-Benz Developer Portal | Non (hors périmètre) | Non (hors périmètre) | Oui (OEM, sous contrat) | Non | Non | Non |
| Ford Developer Portal | Non (hors périmètre) | Non (hors périmètre) | Oui (OEM, sous contrat/consentement) | Non | Non | Non |

Cette matrice met en évidence le constat central de cette étude : **aucune source gratuite et sans inscription ne couvre à la fois le décodage VIN et les spécifications techniques complètes.** NHTSA vPIC couvre parfaitement le premier besoin gratuitement, mais aucune source équivalente n'a été trouvée pour le second, d'où le recours à un jeu de données local/démo pour les spécifications techniques dans le projet.

---

## Conclusion

Cette étude comparative a permis d'évaluer dix sources de données automobiles selon des critères objectifs et vérifiables : méthode d'authentification, modèle tarifaire, existence d'un palier gratuit, et facilité d'accès pour un projet académique sans budget ni partenariat commercial.

**API effectivement intégrées dans le projet :**

1. **NHTSA vPIC** (https://vpic.nhtsa.dot.gov/api/) — utilisée pour le décodage VIN et le référentiel marques/modèles/constructeurs. C'est la seule API de cette étude qui est simultanément gratuite, sans clé, sans inscription, publique et officiellement maintenue par un organisme gouvernemental, ce qui en fait le choix le plus robuste et le plus pérenne pour cette fonctionnalité.
2. **Un fournisseur de données local/démo** pour les spécifications techniques complètes (puissance, couple, dimensions, consommation, émissions de CO2) des 27 véhicules curés — solution retenue tant qu'aucune clé payante n'était disponible.
3. **CarsXE** (https://carsxe.com/) — intégrée en cours de projet grâce à une clé d'essai personnelle fournie par l'utilisateur. Utilisée pour les **photos réelles de véhicules** (`/images`) sur les 27 véhicules du catalogue, avec persistance en base après le premier appel pour préserver le quota d'essai. L'endpoint `/specs` (enrichissement VIN) est implémenté mais non branché sur le flux principal en V1.
4. **API Ninjas — Cars API** (https://api-ninjas.com/api/cars) — intégrée en cours de projet grâce à une clé gratuite fournie par l'utilisateur, sur l'endpoint **`/v1/cars`** (et non `/v2/cars`, verrouillé premium — voir détail par API ci-dessus). Utilisée comme source de secours en direct quand une recherche ne trouve rien dans le catalogue local, avec persistance des résultats trouvés.

Ces deux dernières intégrations restent **secondaires** au jeu de données local/démo : elles enrichissent l'expérience (vraies photos, couverture de marques hors catalogue) sans remettre en cause le choix initial documenté ci-dessus, qui reste valide en mode `DEMO_MODE=true` sans aucune clé configurée.

**API étudiées mais explicitement exclues, avec justification :**

| API | Raison de l'exclusion |
|---|---|
| CarQuery API | Service jugé non fiable/obsolète : base de données non mise à jour depuis 2019, portail de support constaté suspendu lors de la vérification |
| Edmunds API | Programme d'API ouvert retiré depuis 2018 ; inscriptions publiques fermées, accès réservé aux partenaires |
| Marketcheck API | Palier gratuit trop restreint (500 appels/mois) pour un usage réel ; fonctionnalités complètes payantes (à partir d'environ 299 $/mois) |
| Auto.dev | Palier gratuit limité (1 000 appels/mois) ; volumes réels et fonctionnalités étendues payants |
| Mercedes-Benz Developer Portal | Accès OEM restreint : données de véhicules réels nécessitant la possession d'un véhicule Mercedes-Benz ou un accord-cadre contractuel signé |
| Ford Developer Portal | Accès partenaire gated par candidature ; consentement OAuth du propriétaire requis pour les données consommateur |
| API de lookup plaque → véhicule | Aucune solution gratuite, fiable et sans inscription identifiée sur le marché ; fonctionnalité non implémentée plutôt que simulée artificiellement |

CarsXE et API Ninjas figuraient initialement dans cette liste d'exclusion (voir le détail par API ci-dessus, qui documente les deux temps de l'étude) : l'exclusion portait sur leurs offres 100% gratuites, qui restent réellement limitées comme démontré par les tests en conditions réelles. L'obtention de clés par l'utilisateur (essai payant CarsXE, palier gratuit restreint API Ninjas) a permis une intégration réelle et vérifiée, sans jamais fabriquer un contrat d'API non documenté.

Cette démarche de sélection illustre un principe méthodologique central du projet : privilégier des sources de données **vérifiables, gratuites et pérennes** plutôt que de dépendre de services tiers payants, instables ou à accès restreint, tout en documentant honnêtement les limites rencontrées plutôt que de les contourner par des données fictives.

---

## Sources consultées

- https://vpic.nhtsa.dot.gov/api/
- https://vpic.nhtsa.dot.gov/
- https://vpic.nhtsa.dot.gov/decoder/
- https://www.carqueryapi.com/
- https://support.carqueryapi.com/
- https://developer.edmunds.com/
- https://help.edmunds.com/hc/en-us/articles/4414038118679-Edmunds-Developer-API
- https://www.marketcheck.com/apis/pricing/
- https://docs.marketcheck.com/docs/get-started/api/authentication
- https://docs.marketcheck.com/docs/get-started/api/introduction
- https://carsxe.com/
- https://docs.carsxe.com/docs
- https://api.carsxe.com/
- https://www.auto.dev/
- https://www.auto.dev/pricing
- https://docs.auto.dev/v2
- https://api-ninjas.com/api/cars
- https://api-ninjas.com/pricing
- https://developer.mercedes-benz.com/
- https://connectivity.mercedes-benz.com/products/mercedes-benz-fleet-api
- https://developer.ford.com/
- https://developer.ford.com/apis
- https://developer.ford.com/apis/fordconnect-query
- https://vingurus.com/license-plate-lookup
- https://vehiclelookup.org/
- https://forcar.org/license-plate/
- https://zilocar.com/license-plate-lookup
- https://vehicledatabases.com/license-plate-api

*Note méthodologique : les montants et quotas indiqués proviennent des pages officielles de tarification ou de documentation consultées à la date de rédaction (septembre 2026). Les tarifs des API commerciales étant susceptibles d'évoluer, il est recommandé de revérifier les pages de tarification citées avant toute décision d'intégration définitive.*
