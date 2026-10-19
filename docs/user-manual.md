# Manuel utilisateur — Vehicle Intelligence Platform

## Page d'accueil (`/`)

Présente la plateforme, ses cinq modules, le processus (Search → Retrieve →
Decode → Analyze → Compare) et les sources de données. Une barre de
recherche rapide envoie directement vers `/search`.

## Recherche de véhicules (`/search`)

1. Saisissez un terme libre (marque, modèle, constructeur) et/ou utilisez les
   filtres : marque et modèle avec autocomplétion (source NHTSA en temps
   réel avec repli sur le catalogue local si l'API est indisponible), année,
   carburant.
2. Les filtres sont **combinables** — ex. Marque=BMW + Carburant=Diesel.
3. Chaque résultat s'affiche en carte avec badge **Demo data** ou **Live
   API** selon la provenance des champs affichés.
4. Cliquez sur l'icône de comparaison pour ajouter un véhicule à une
   comparaison (jusqu'à 4), puis sur "Comparer" une fois au moins 2
   sélectionnés.
5. Aucun résultat : un message explicite s'affiche avec une action pour
   réinitialiser les filtres.

## Fiche véhicule (`/vehicles/:id`)

Fiche complète organisée en sections : Identification, Motorisation,
Performances, Dimensions, Consommation, Environnement, Équipements &
sécurité. Toute donnée non disponible auprès de la source affiche
littéralement **"Data not available"** — jamais une valeur inventée ou un
champ vide ambigu.

Le lien "Afficher la traçabilité des sources" liste, champ par champ, le
fournisseur, l'endpoint et la date de récupération de la donnée.

## Décodeur VIN (`/vin-decoder`)

1. Saisissez un VIN de 17 caractères (alphanumérique, sans I/O/Q).
2. Une erreur de format s'affiche immédiatement côté client ; la validation
   est **revalidée côté serveur** dans tous les cas.
3. Le décodage interroge l'API réelle NHTSA vPIC. Le résultat affiche un
   badge **Live API**, et toute donnée que NHTSA ne fournit pas pour ce VIN
   apparaît comme "Data not available".

## Comparaison (`/compare`)

1. Recherchez et ajoutez 2 à 4 véhicules.
2. Le tableau comparatif met en évidence, ligne par ligne, la **meilleure
   valeur** — la logique "plus c'est mieux" ou "moins c'est mieux" est
   appliquée automatiquement selon le critère (puissance ↑, consommation ↓,
   CO₂ ↓, etc.).
3. Un radar chart offre une vue synthétique normalisée (0-100) des critères
   clés.
4. Si vous êtes connecté, un bouton "Sauvegarder cette comparaison" permet de
   la retrouver plus tard.

## Dashboard (`/dashboard`, connexion requise)

Statistiques personnelles : nombre de recherches, VIN décodés, véhicules
consultés, comparaisons sauvegardées ; graphiques de puissance moyenne et
d'émissions CO₂ moyennes par marque (sur l'ensemble du catalogue), et
répartition de vos recherches par carburant / dans le temps.

## Historique (`/history`, connexion requise)

Journal chronologique de toutes vos actions : recherches filtrées, VIN
décodés, fiches consultées, avec horodatage.

## Comparaisons sauvegardées (`/saved-comparisons`, connexion requise)

Consultez, renommez ou supprimez vos comparaisons enregistrées.

## Compte (`/login`, `/register`, `/profile`)

Inscription par email/mot de passe (8 caractères minimum), connexion par
JWT, modification du nom ou du mot de passe depuis le profil. Les routes
sensibles (dashboard, historique, comparaisons sauvegardées, profil)
redirigent automatiquement vers `/login` si vous n'êtes pas connecté, et vous
ramènent à la page demandée après connexion.

## Mode démonstration

Toute donnée technique complète (puissance, couple, consommation, CO₂...)
provient d'un jeu de données local clairement marqué **Demo data** — c'est
la seule solution gratuite et sans clé pour ces champs (voir
`api-comparison.md`). Le décodage VIN, lui, est **toujours réel** (API
NHTSA), jamais simulé.
