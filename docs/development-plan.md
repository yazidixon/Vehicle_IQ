# Plan de développement — Vehicle Intelligence Platform

## 1. Analyse du cahier des charges

Le projet demande une plateforme web locale de recherche, décodage, analyse et
comparaison de véhicules, avec :

- une API de décodage VIN réelle,
- une source de spécifications techniques,
- une base de données PostgreSQL faisant office de stockage + cache,
- une authentification complète (JWT),
- un dashboard analytique, un module de comparaison, un historique,
- une exécution 100% locale (aucun déploiement cloud), avec Docker en option
  de confort.

### Décisions de cadrage

| Sujet | Décision | Justification |
|---|---|---|
| API VIN réelle | **NHTSA vPIC** (`vpic.nhtsa.dot.gov`) | Seule API de l'étude qui est publique, gratuite, sans clé, stable et documentée officiellement (gouvernement US). Voir `docs/api-comparison.md`. |
| Spécifications techniques complètes (puissance, couple, conso, CO₂, dimensions) | **Jeu de données local "Demo Provider"**, clairement étiqueté `Demo data` | Aucune API listée dans le cahier des charges n'offre ces données sans clé payante ou compte partenaire. Conforme à la section 31 (Mode démonstration). |
| Photos de véhicules | **CarsXE** (`/images`), clé fournie par l'utilisateur, résultat persisté après le premier appel | Ajouté en cours de projet : aucune API gratuite/keyless d'images fiables trouvée (voir `docs/api-comparison.md`) ; sans clé, l'app reste élégante avec un pictogramme de remplacement (section 32). |
| Recherche de secours hors catalogue démo | **API Ninjas** `/v1/cars` (endpoint gratuit, pas `/v2/cars` qui est payant), clé fournie par l'utilisateur | Ajouté en cours de projet pour couvrir les marques/modèles absents des 27 véhicules curés, avec des données réelles plutôt qu'un résultat vide. |
| Recherche par plaque d'immatriculation | Non branchée sur une vraie source (message explicite "non disponible sans API tierce payante") | Aucune API plaque→véhicule gratuite et légale n'a été trouvée ; on respecte la règle "ne jamais fabriquer une réponse API". |
| Backend | **Spring Boot 4 (Java 17) + Spring Data JPA + Spring Security + PostgreSQL** | Choix révisé en cours de projet à la demande explicite du commanditaire (voir §2). |
| Frontend | React + TypeScript + Vite + Tailwind CSS v4 + Recharts + React Router | Conservé du cahier des charges initial. |
| Auth | JWT (HMAC) + BCrypt | Imposé, standard éprouvé, implémenté via un filtre `JwtAuthenticationFilter` + `SecurityFilterChain` Spring Security. |
| Cache | Table `api_cache` en PostgreSQL (provider, endpoint, params_hash, payload JSON, created_at, expires_at) | Cahier des charges section 17. |
| Docker | `docker-compose.yml` (frontend, backend, postgres), local uniquement | Section 34. |

## 2. Changement de stack en cours de projet

Le projet a été initialement développé avec un backend **FastAPI/Python**
(conforme à la stack par défaut suggérée par le cahier des charges), avec un
backend fonctionnel complet (auth, VIN decode réel, recherche, comparaison,
cache, dashboard) validé par des tests automatisés.

À la demande explicite de l'utilisateur en cours de développement, le backend
a été **entièrement réécrit en Spring Boot 4 / Java 17**, en conservant :

- le même contrat d'API REST (mêmes routes, mêmes noms de champs JSON en
  `snake_case` côté client) pour que le frontend React déjà développé n'ait
  besoin d'aucune modification de logique métier ;
- la même architecture en couches (contrôleurs → services → adapters/
  providers → base de données) ;
- le même jeu de données de démonstration curé (27 véhicules), migré du
  format Python vers un fichier JSON partagé (`backend/src/main/resources/
  data/demo_vehicles.json`) ;
- la même étude comparative des APIs, indépendante du langage backend.

Le frontend (React/Vite/Tailwind) n'a **pas** été réécrit — seule une
correction de bug mineure a été nécessaire (un champ mal nommé lors de
l'inscription). Ceci confirme la solidité du contrat d'API défini dès la V1.

## 3. Phasage réel exécuté

1. **Phase 1** — Analyse + document de planification.
2. **Phase 2** — Squelette frontend / backend / DB / Docker / docs.
3. **Phase 3** — Modèles de base de données + migrations.
4. **Phase 4** — Backend : services, adapters (NHTSA + demo), repositories, routes API, sécurité, cache, gestion d'erreurs, logging.
5. **Phase 5** — Frontend : layouts, pages, composants, contexts, services API, graphiques.
6. **Phase 6** — Connexion frontend ↔ backend (Axios, CORS, intercepteurs JWT).
7. **Phase 7** — Intégration API réelle (NHTSA) + normalisation des données.
8. **Phase 8** — Cache + fallback DEMO_MODE quand une API est indisponible.
9. **Phase 9** — Réécriture backend en Spring Boot (changement de stack demandé), migration des données de démonstration, re-validation de tous les endpoints.
10. **Phase 10** — Tests backend (JUnit 5 + AssertJ + TestRestTemplate) + tests frontend (Vitest) + corrections de bugs réels découverts en testant (voir `docs/technical-decisions.md`).
11. **Phase 11** — Polish UI/UX (skeletons, animations, responsive, badges de source).
12. **Phase 12** — Documentation complète (`docs/`) + validation de bout en bout.

## 4. Périmètre explicitement exclu (V1)

Conformément aux sections 39-40 du cahier des charges : pas de diagnostic IA,
pas de maintenance prédictive, pas de gestion de flotte, pas d'estimation de
valeur marchande, pas d'OBD-II, pas de marketplace/paiement. Ces éléments sont
listés dans `docs/technical-decisions.md` comme perspectives futures uniquement.
