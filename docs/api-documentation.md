# Documentation API — Vehicle Intelligence Platform

Base URL locale : `http://localhost:8000/api`
Documentation interactive (Swagger UI) : `http://localhost:8000/swagger-ui/index.html`
Spécification OpenAPI brute : `http://localhost:8000/v3/api-docs`

Toutes les réponses sont en JSON, champs en `snake_case`. Les erreurs suivent
le format commun :

```json
{ "error": "validation_error", "message": "Les données envoyées sont invalides.", "details": { "vin": "..." } }
```

## Authentification

| Méthode | Route | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | non | Créer un compte, retourne un JWT |
| POST | `/api/auth/login` | non | Connexion, retourne un JWT |
| GET | `/api/auth/me` | requise | Profil de l'utilisateur connecté |
| PUT | `/api/auth/me` | requise | Modifier nom / mot de passe |

**POST /api/auth/register**
```json
// Requête
{ "email": "alice@example.com", "full_name": "Alice Dupont", "password": "SuperSecret123" }
// Réponse 201
{
  "access_token": "eyJ...",
  "token_type": "bearer",
  "user": { "id": "…", "email": "alice@example.com", "full_name": "Alice Dupont", "is_active": true, "created_at": "2026-09-09T16:34:14Z" }
}
```
Le JWT s'utilise ensuite dans l'en-tête `Authorization: Bearer <token>`.

## Véhicules

| Méthode | Route | Auth | Description |
|---|---|---|---|
| GET | `/api/vehicles` | optionnelle | Recherche paginée avec filtres combinables |
| GET | `/api/vehicles/search` | optionnelle | Alias de `/api/vehicles` (conformité cahier des charges) |
| GET | `/api/vehicles/meta/makes` | non | Liste des marques (autocomplétion, source NHTSA) |
| GET | `/api/vehicles/meta/models?make=` | non | Liste des modèles d'une marque |
| GET | `/api/vehicles/{id}` | optionnelle | Fiche technique complète |
| GET | `/api/vehicles/{id}/sources` | non | Traçabilité champ par champ |

**GET /api/vehicles** — paramètres de requête (tous optionnels, combinables) :

| Paramètre | Type | Exemple |
|---|---|---|
| `q` | texte libre (marque, modèle, constructeur) | `Tesla Model 3` |
| `make` | égalité exacte, insensible à la casse | `BMW` |
| `model` | contient (insensible à la casse) | `Serie 3` |
| `manufacturer` | égalité exacte | `Toyota` |
| `year` | égal | `2023` |
| `year_min`, `year_max` | plage | `2020` / `2024` |
| `fuel_type` | contient | `Diesel` |
| `engine_code` | contient | `B47D20` |
| `page`, `page_size` | pagination (défaut 1 / 12) | |

Réponse : `PaginatedResponse<VehicleSummary>` — `{ items, total, page, page_size, total_pages }`.

## Décodage VIN

| Méthode | Route | Auth | Description |
|---|---|---|---|
| POST | `/api/vin/decode` | optionnelle | Décodage réel via NHTSA vPIC |

```json
// Requête
{ "vin": "1HGCM82633A004352" }
// Réponse 200
{
  "vin": "1HGCM82633A004352", "is_valid": true,
  "country_of_manufacture": "UNITED STATES (USA)",
  "manufacturer": "AMERICAN HONDA MOTOR CO., INC.",
  "plant": "MARYSVILLE", "model_year": "2003",
  "make": "HONDA", "model": "Accord", "series": "Data not available",
  "trim": "EX-V6", "engine_code": "V-Shaped", "body_class": "Coupe",
  "source_provider": "nhtsa", "is_demo_data": false, "warnings": []
}
```
Erreurs : `422` si le format VIN est invalide (longueur ≠ 17, ou contient I/O/Q) ;
`502` si NHTSA est injoignable ; `503` si le provider n'est pas configuré.

## Comparaison

| Méthode | Route | Auth | Description |
|---|---|---|---|
| POST | `/api/comparisons/compute` | non | Calcule une comparaison (2 à 4 véhicules), sans sauvegarde |
| POST | `/api/comparisons` | requise | Sauvegarde une comparaison nommée |
| GET | `/api/comparisons` | requise | Liste des comparaisons sauvegardées |
| GET | `/api/comparisons/{id}` | requise | Détail d'une comparaison sauvegardée |
| PUT | `/api/comparisons/{id}` | requise | Renommer |
| DELETE | `/api/comparisons/{id}` | requise | Supprimer |

**POST /api/comparisons/compute**
```json
{ "vehicle_ids": ["uuid1", "uuid2"] }
```
Réponse : liste de véhicules complets + liste de métriques, chacune avec
`better_direction` (`higher` | `lower` | `neutral`) et `best_vehicle_id` déjà
calculé — le frontend n'a qu'à surligner la valeur gagnante.

## Historique et dashboard

| Méthode | Route | Auth | Description |
|---|---|---|---|
| GET | `/api/search/history` | requise | Historique des recherches / VIN décodés / consultations |
| GET | `/api/dashboard` | requise | Statistiques agrégées + séries pour les graphiques |

## Codes d'erreur HTTP utilisés

| Code | Cas |
|---|---|
| 400 | Requête malformée |
| 401 | Authentification manquante ou token invalide/expiré |
| 404 | Ressource introuvable |
| 409 | Conflit (email déjà utilisé) |
| 422 | Échec de validation (VIN, champs de formulaire) |
| 429 | Quota dépassé *(réservé, non déclenché par les providers actuels)* |
| 502 | Fournisseur externe indisponible |
| 503 | Fournisseur non configuré |
| 500 | Erreur interne inattendue |

Tous les cas sont gérés par un `@RestControllerAdvice` centralisé
(`GlobalExceptionHandler`), qui journalise les erreurs serveur (5xx) en
niveau `ERROR` et les erreurs client (4xx) en `INFO`, sans jamais logguer un
mot de passe, un token ou une clé API.
