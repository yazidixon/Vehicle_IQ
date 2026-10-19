# Installation et lancement local

Ce projet s'exécute **entièrement en local**. Rien n'est déployé sur un
service cloud.

## Prérequis

- **Java 17** (JDK)
- **Node.js 20+** et npm
- **PostgreSQL 16** — via Docker, une installation locale, ou le script de
  secours fourni (voir §3)
- **Docker + Docker Compose** (optionnel, pour l'option "tout en un")

## 1. Configuration

```bash
cp .env.example .env
```

Adapter au besoin `.env` — les valeurs par défaut fonctionnent telles quelles
pour une démonstration locale (`DEMO_MODE=true`, aucune clé API tierce
requise). Générer un vrai secret JWT avant toute démonstration publique :

```bash
openssl rand -base64 48
```

## 2. Option A — Tout lancer avec Docker Compose

```bash
docker compose up --build
```

Démarre PostgreSQL, le backend Spring Boot (build Maven multi-stage) et le
frontend (serveur de dev Vite) dans trois conteneurs reliés par un réseau
Docker dédié. Accès :

- Frontend : http://localhost:5173
- Backend : http://localhost:8000
- Documentation API : http://localhost:8000/swagger-ui/index.html

## 3. Option B — Lancement natif (sans Docker)

### 3.1. PostgreSQL

**Si PostgreSQL est déjà installé** (service natif ou via un gestionnaire de
paquets), créez simplement la base et le rôle :

```sql
CREATE ROLE vehicle_admin LOGIN PASSWORD 'change_me_locally' SUPERUSER;
CREATE DATABASE vehicle_platform OWNER vehicle_admin;
```

**Si vous n'avez pas de PostgreSQL installé et pas les droits administrateur**
(cas rencontré lors du développement de ce projet), un script de secours est
fourni dans `docker/pg-embedded/` : il lance un vrai serveur PostgreSQL 16 en
espace utilisateur (aucun droit admin requis), via le paquet Python
`pgserver`, sur le port standard 5432.

```bash
cd docker/pg-embedded
python -m venv .venv
./.venv/Scripts/pip install pgserver      # Windows ; sous Linux/Mac : .venv/bin/pip
./.venv/Scripts/python -u run_pg.py       # reste au premier plan, Ctrl+C pour arrêter
```

Le script crée automatiquement le rôle `vehicle_admin` et les bases
`vehicle_platform` / `vehicle_platform_test`, et corrige au premier lancement
un bug connu du paquet sur Windows (données de fuseau horaire manquantes) en
récupérant une fois les données nécessaires depuis un artefact Maven public
— voir les commentaires en tête de `run_pg.py` pour le détail.

### 3.2. Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Au premier démarrage, le schéma est créé automatiquement et les données de
démonstration sont insérées (27 véhicules, registre des sources API). Le
backend est prêt quand `http://localhost:8000/health` répond `{"status":"ok"}`.

### 3.3. Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

Accès : http://localhost:5173

## 4. Comptes de démonstration

Aucun compte n'est pré-créé en base (conformité RGPD/propreté des données de
démo) — créez un compte via `/register`, l'inscription est immédiate et ne
nécessite aucune validation d'email.

## 5. Tests

**Backend** :
```bash
cd backend
./mvnw test
```
31 tests (JUnit 5 + AssertJ + TestRestTemplate), y compris un test
d'intégration réel contre l'API NHTSA (nécessite un accès Internet — c'est la
même API que l'application utilise en fonctionnement normal).

**Frontend** :
```bash
cd frontend
npm run test
```

## 6. Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| Le backend ne démarre pas, erreur JDBC | PostgreSQL non démarré ou identifiants incorrects | Vérifier `DATABASE_URL` dans `.env` et que PostgreSQL écoute bien sur le port 5432 |
| `422` sur toutes les recherches de véhicule par filtre `year` | Ancienne base migrée avant le renommage de colonne `year` → `model_year` | Recréer la base (`DROP DATABASE` puis relancer le backend) |
| Le décodage VIN échoue systématiquement (`502`) | Pas d'accès Internet, ou NHTSA temporairement indisponible | Réessayer plus tard ; NHTSA est un service public sans SLA garanti |
| CORS bloqué dans la console navigateur | `CORS_ORIGINS` ne correspond pas à l'URL du frontend | Vérifier que `CORS_ORIGINS=http://localhost:5173` dans `.env` du backend |
