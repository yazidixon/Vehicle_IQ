# VehicleIQ — Vehicle Intelligence Platform

> Explore. Decode. Analyze. Compare.

Plateforme web de recherche, décodage VIN, analyse et comparaison de
véhicules, développée dans le cadre d'un **projet académique**. Exécution
**100% locale** — aucun déploiement cloud.

## Objectifs

Centraliser autant que possible l'information disponible sur un véhicule à
partir de sources automobiles réelles : décodage VIN officiel, fiche
technique détaillée, dashboard analytique et comparaison intelligente,
avec traçabilité complète des sources et aucune donnée inventée.

## Fonctionnalités

- Recherche de véhicules par marque / modèle / année / carburant / texte libre, filtres combinables, autocomplétion, pagination
- Décodage VIN **réel** via l'API officielle NHTSA vPIC, validation stricte (17 caractères, alphanumérique, sans I/O/Q)
- Fiche technique complète (identification, motorisation, performances, dimensions, consommation, environnement, équipements, sécurité)
- Traçabilité des sources champ par champ (fournisseur, endpoint, date de récupération)
- Dashboard analytique (statistiques + graphiques Recharts)
- Comparaison de 2 à 4 véhicules avec mise en évidence automatique de la meilleure valeur (logique "plus/moins c'est mieux" par critère) et vue radar synthétique
- Historique des recherches et comparaisons sauvegardées
- Authentification JWT (inscription, connexion, profil, routes protégées)
- Cache PostgreSQL des réponses API externes (réduction des appels redondants)
- Mode démonstration (`DEMO_MODE`) clairement étiqueté, jamais confondu avec une donnée réelle
- Interface responsive (desktop / tablette / mobile), design automobile moderne

## Architecture

```text
Frontend (React)  →  Backend (Spring Boot)  →  Providers (NHTSA / Demo)
                              ↓
                        PostgreSQL (données + cache)
```

Détails complets : [`docs/architecture.md`](docs/architecture.md).

## Stack technique

| Couche | Technologies |
|---|---|
| Frontend | React 19, TypeScript, Vite, Tailwind CSS v4, React Router, Axios, Recharts, Lucide React |
| Backend | Java 17, Spring Boot 4, Spring Data JPA, Spring Security, JJWT, Hibernate 7 |
| Base de données | PostgreSQL 16 |
| Infrastructure locale | Docker, Docker Compose |
| Tests | JUnit 5 + AssertJ + TestRestTemplate (backend), Vitest + Testing Library (frontend) |

## Prérequis

- Java 17, Node.js 20+, PostgreSQL 16 (ou Docker)

## Installation et lancement

```bash
cp .env.example .env
docker compose up --build
```

Ou en lancement natif (voir [`docs/installation.md`](docs/installation.md)
pour le détail, y compris une solution de secours PostgreSQL sans droits
administrateur) :

```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Frontend (autre terminal)
cd frontend && npm install && npm run dev
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8000 |
| Documentation API (Swagger) | http://localhost:8000/swagger-ui/index.html |

## Configuration (`.env`)

Voir [`.env.example`](.env.example) — valeurs par défaut fonctionnelles pour
une démonstration locale sans aucune clé API tierce (`DEMO_MODE=true`).

## APIs automobiles

| API | Statut | Usage |
|---|---|---|
| NHTSA vPIC | ✅ Intégrée (réelle, gratuite, sans clé) | Décodage VIN, listes marque/modèle |
| Jeu de données local | ✅ Intégré (mode démo, clairement étiqueté) | Spécifications techniques complètes |
| CarQuery, Edmunds, Marketcheck, CarsXE, API Ninjas, Auto.dev, Mercedes-Benz/Ford Dev Portal | 📋 Étudiées, non intégrées (payantes / fermées / instables) | — |

Étude comparative complète : [`docs/api-comparison.md`](docs/api-comparison.md).

## Tests

```bash
cd backend && ./mvnw test     # 31 tests (JUnit 5 + AssertJ)
cd frontend && npm run test   # 20 tests (Vitest + Testing Library)
```

## Structure du projet

```text
vehicle-platform/
├── frontend/           React + TypeScript + Vite
├── backend/            Spring Boot 4 (Java 17)
│   └── src/main/java/com/vehicleplatform/
│       ├── controller/   Contrôleurs REST
│       ├── service/      Logique métier
│       ├── adapter/      Abstraction des providers externes (NHTSA, Demo)
│       ├── repository/   Accès données (Spring Data JPA)
│       ├── entity/       Entités JPA
│       ├── dto/          Objets de transfert (records)
│       ├── security/     JWT
│       └── config/       Configuration Spring
├── docs/               Documentation complète
├── docker/             Dockerfiles et outils locaux
├── docker-compose.yml
└── .env.example
```

## Documentation

- [Plan de développement](docs/development-plan.md)
- [Architecture](docs/architecture.md)
- [Base de données](docs/database.md)
- [Documentation API](docs/api-documentation.md)
- [Étude comparative des APIs](docs/api-comparison.md)
- [Installation](docs/installation.md)
- [Manuel utilisateur](docs/user-manual.md)
- [Décisions techniques](docs/technical-decisions.md)

## Hors périmètre (V1)

Diagnostic IA, maintenance prédictive, gestion de flotte, estimation de
valeur, OBD-II temps réel, marketplace/paiement — voir
[`docs/technical-decisions.md`](docs/technical-decisions.md#5-hors-périmètre-perspectives-futures).

## Licence

Projet académique — usage éducatif uniquement.
