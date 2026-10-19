# Base de données — Vehicle Intelligence Platform

Moteur : **PostgreSQL 16**. ORM : **Spring Data JPA / Hibernate 7**, schéma
géré automatiquement en développement (`spring.jpa.hibernate.ddl-auto=update`).

## 1. Modèle entité-relation

```mermaid
erDiagram
    USERS ||--o{ SEARCH_HISTORY : "effectue"
    USERS ||--o{ SAVED_COMPARISONS : "sauvegarde"
    USERS ||--o{ VIN_DECODES : "décode (optionnel)"

    VEHICLES ||--o| VEHICLE_SPECIFICATIONS : "possède"
    VEHICLES ||--o{ VEHICLE_SOURCE_DATA : "traçabilité par champ"

    USERS {
        uuid id PK
        string email UK
        string full_name
        string hashed_password
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    VEHICLES {
        uuid id PK
        string manufacturer
        string make
        string model
        string generation
        int model_year
        string trim
        string body_type
        string fuel_type
        string engine_code
        string source_provider
        boolean demo_data
        timestamp created_at
        timestamp updated_at
    }

    VEHICLE_SPECIFICATIONS {
        uuid id PK
        uuid vehicle_id FK
        string engine_type
        int engine_displacement_cc
        int horsepower_hp
        int torque_nm
        string transmission
        string drivetrain
        int top_speed_kmh
        double acceleration_0_100_s
        int length_mm
        int width_mm
        int height_mm
        int wheelbase_mm
        int weight_kg
        int doors
        int seats
        double fuel_consumption_urban_l100km
        double fuel_consumption_extra_urban_l100km
        double fuel_consumption_mixed_l100km
        int co2_emissions_gkm
        string euro_norm
        string energy_class
        double price_msrp
        string currency
        text safety_notes
        boolean demo_data
    }

    VEHICLE_SOURCE_DATA {
        uuid id PK
        uuid vehicle_id FK
        string field_name
        text field_value
        string source_provider
        string source_endpoint
        boolean demo_data
        timestamp retrieved_at
    }

    VIN_DECODES {
        uuid id PK
        uuid user_id FK "nullable"
        string vin
        string country_of_manufacture
        string manufacturer
        string plant
        string model_year
        string make
        string model
        string series
        string trim
        string engine_code
        string body_class
        text raw_payload
        string source_provider
        boolean demo_data
        timestamp created_at
    }

    API_SOURCES {
        uuid id PK
        string name UK
        string base_url
        text description
        boolean requires_key
        boolean configured
        boolean active
        timestamp created_at
    }

    API_CACHE {
        uuid id PK
        string provider
        string endpoint
        string params_hash
        text params
        text response
        timestamp created_at
        timestamp expires_at
    }

    SEARCH_HISTORY {
        uuid id PK
        uuid user_id FK
        string search_type
        text query_params
        string result_summary
        timestamp created_at
    }

    SAVED_COMPARISONS {
        uuid id PK
        uuid user_id FK
        string name
        timestamp created_at
        timestamp updated_at
    }
```

`SAVED_COMPARISONS.vehicle_ids` est une collection d'éléments (`@ElementCollection`)
stockée dans une table associée `saved_comparison_vehicles(comparison_id, position, vehicle_id)`,
ce qui préserve l'ordre choisi par l'utilisateur.

## 2. Notes de conception

- **Clé primaire** : UUID généré côté application (`@UuidGenerator`), pas de
  séquence entière — évite toute fuite d'information sur le volume de
  données et facilite une éventuelle réplication.
- **`vehicles.model_year`** (et non `year`) : `year` est un mot réservé dans
  certains dialectes SQL (H2, utilisé pour les tests) et casse les requêtes
  non échappées ; le nom de colonne a été choisi pour rester portable.
- **Traçabilité (`vehicle_source_data`)** : chaque champ technique important
  (puissance, couple, consommation, CO₂, poids, prix) a sa propre ligne de
  provenance, avec le fournisseur, l'endpoint et la date de récupération —
  c'est ce qui alimente le bandeau "Source : NHTSA / Provider X" et le badge
  `Demo data` / `Live API` sur chaque valeur.
- **Cache (`api_cache`)** : indexé sur `(provider, endpoint, params_hash)`.
  `params_hash` est un SHA-256 des paramètres de requête canonicalisés
  (triés), ce qui permet une clé de cache stable indépendamment de l'ordre
  des paramètres.
- **Historique (`search_history`)** : `search_type` vaut `filter`, `vin` ou
  `vehicle_view` ; `query_params` est un JSON sérialisé permettant de
  reconstituer les filtres exacts utilisés (alimente le dashboard : nombre
  de recherches, répartition par carburant, activité dans le temps).
- **`is_demo_data` / `demo_data`** : présent sur `vehicles`,
  `vehicle_specifications`, `vehicle_source_data` et `vin_decodes` — jamais
  une donnée réelle n'est mélangée silencieusement avec une donnée de
  démonstration ; le frontend affiche systématiquement le badge correspondant.

## 3. Contraintes et index

| Table | Contrainte / Index |
|---|---|
| `users` | `UNIQUE(email)` |
| `vehicles` | index sur `manufacturer`, `make`, `model`, `model_year`, `fuel_type`, `external_id` |
| `vehicle_specifications` | `UNIQUE(vehicle_id)` (relation 1-1) |
| `api_cache` | index composite `(provider, endpoint, params_hash)` |
| `search_history` | index sur `(user_id, created_at)` implicite via la requête triée |
| Toutes les tables avec FK | `ON DELETE CASCADE` pour les entités "détail" (spécifications, sources, historique, comparaisons) rattachées à un parent supprimé ; `ON DELETE SET NULL` pour `vin_decodes.user_id` (un décodage VIN anonyme reste valide) |

## 4. Migrations

Le schéma est actuellement géré par Hibernate (`ddl-auto=update`), adapté à
un contexte académique où la base est recréée facilement. Pour une évolution
vers un contrôle de version de schéma explicite, `flyway-core` ou
`liquibase-core` s'intègrent nativement à Spring Boot sans changer le reste
de l'architecture (piste documentée dans `technical-decisions.md`).
