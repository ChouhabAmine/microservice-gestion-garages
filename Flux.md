# 🚗 Projet Renault Garage — Architecture Hexagonale (In-Memory)

Le projet **Renault Garage** suit une **architecture hexagonale** (ports et adaptateurs) basée sur **Spring WebFlux**, **Kafka**, et des **données en mémoire** à l’aide de `ConcurrentHashMap`.  
Aucune base de données externe (comme H2) n’est utilisée : toutes les entités sont stockées et gérées en mémoire.

---

## ⚙️ Pré-requis

Avant de tester les endpoints liés aux véhicules, il est nécessaire de **créer le topic Kafka** `vehicle-created`.

### Étapes à suivre :

1. Démarrer l’environnement Docker :  
   ```bash
   docker compose up -d
   ```

2. Créer le topic Kafka `vehicle-created` :  
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --create --topic vehicle-created --partitions 1 --replication-factor 1
   ```

3. Vérifier la liste des topics existants :  
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

---

## 🏢 Garages

### 1️⃣ Ajout d’un garage
**Endpoint :**  
`POST http://localhost:8080/garages`

**Corps JSON :**
```json
{
  "id": "g1",
  "name": "Renault Casa Sud",
  "address": "Boulevard Mohammed V, Casablanca",
  "telephone": "0611223375",
  "email": "contact.casa@example.com"
}
```

---

### 2️⃣ Récupération d’un garage par ID
**Endpoint :**  
`GET http://localhost:8080/garages/g1`

---

### 3️⃣ Mise à jour d’un garage
**Endpoint :**  
`PATCH http://localhost:8080/garages/g1`

**Corps JSON :**
```json
{
  "id": "g1",
  "name": "Renault Casa Center",
  "address": "Boulevard Mohammed V, Casablanca",
  "telephone": "0611223344",
  "email": "contact.casa@example.com"
}
```

---

### 4️⃣ Suppression d’un garage
**Endpoint :**  
`DELETE http://localhost:8080/garages/g1`

---

### 5️⃣ Liste paginée des garages (tri par nom ascendant)
**Endpoint :**  
`GET http://localhost:8080/garages?sort=name,asc&page=0&size=2`

---

## 🚙 Véhicules

> ⚠️ **Important :** Avant toute opération sur les véhicules, assure-toi que le topic Kafka `vehicle-created` est bien créé.

---

### 6️⃣ Ajout d’un véhicule
**Endpoint :**  
`POST http://localhost:8080/garages/g1/vehicles`

**Corps JSON :**
```json
{
  "id": "v1",
  "brand": "Renault",
  "model": "Clio 5",
  "year": 2023,
  "mileage": 15000,
  "status": "AVAILABLE",
  "fuelType": "PETROL"
}
```

---

### 7️⃣ Récupération d’un véhicule par ID
**Endpoint :**  
`GET http://localhost:8080/vehicles/v1`

---

### 8️⃣ Mise à jour d’un véhicule
**Endpoint :**  
`PUT http://localhost:8080/vehicles/v1`

**Corps JSON :**
```json
{
  "id": "v1",
  "brand": "Renault",
  "model": "Clio 5",
  "year": 2023,
  "mileage": 18000,
  "status": "IN_SERVICE",
  "fuelType": "DIESEL"
}
```

---

### 9️⃣ Suppression d’un véhicule
**Endpoint :**  
`DELETE http://localhost:8080/vehicles/v1`

---

### 🔟 Liste des véhicules par modèle
**Endpoint :**  
`GET http://localhost:8080/vehicles/search?model=Clio 5`

Les endpoints de gestion des accessoires sont implémentés dans la classe AccessoryController.

Les endpoints de recherche (requêtes de filtrage) sont disponibles dans la classe GarageSearchController.