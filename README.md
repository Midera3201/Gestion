# SIRH scolaire — Gestion du recrutement et des ressources humaines

Application web de type **SIRH** (Système d'Information des Ressources Humaines)
pour un établissement scolaire : circuit de recrutement complet, de la demande
d'un manager jusqu'à la création du dossier employé et de son contrat.

## 1. Technologies

| Élément | Version / outil |
|---------|-----------------|
| Langage | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Build | Maven |
| Base de données | PostgreSQL 17 |
| Persistance | Spring Data JPA / Hibernate |
| Sécurité | Spring Security (rôles + pages protégées) |
| Vues | Thymeleaf + Bootstrap 5 |
| Tests | JUnit 5, MockMvc, Spring Security Test |

## 2. Prérequis

- JDK 21
- Maven 3.9+
- PostgreSQL 17 en service, avec la base et l'utilisateur suivants :

```
base      : rh
utilisateur : rh
mot de passe : rh
```

## 3. Mise en route

### a) Créer la base et les données de test

```powershell
psql -U rh -d rh -f sql\01_schema.sql
psql -U rh -d rh -f sql\02_data.sql
```

### b) Lancer l'application

```powershell
mvn spring-boot:run
```

L'application écoute sur **http://localhost:4200**
(le port 8080 est souvent déjà occupé, on utilise 4200).

### c) Lancer les tests

```powershell
mvn clean install
```

## 4. Comptes de test

Mot de passe identique pour tous : **`midera2026`**

| E-mail | Rôle | Rôle métier |
|--------|------|-------------|
| `admin@ecole.fr` | ADMIN | Comptes, rôles, historique |
| `manager@ecole.fr` | MANAGER | Crée les demandes de recrutement |
| `rh@ecole.fr` | RH | Vérifie, publie les offres, suit les dossiers |
| `finance@ecole.fr` | FINANCE | Vérifie le budget |
| `direction@ecole.fr` | DIRECTION | Décide les demandes et les embauches |

## 5. Circuit de recrutement

```
Demande (MANAGER)
   EN_ATTENTE_RH -> EN_ATTENTE_FINANCE -> EN_ATTENTE_DIRECTION -> ACCEPTEE
                            (ou REFUSEE, qui arrête le circuit)

Offre (RH) : BROUILLON -> PUBLIEE -> CLOTUREE

Candidature : RECUE -> EN_ENTRETIEN -> EVALUEE -> RETENUE
              -> OFFRE_ENVOYEE -> OFFRE_ACCEPTEE -> EMBAUCHEE

Employé (RH) : dossier + contrat + tâches d'intégration
```

## 6. Séparation des pouvoirs

- L'**ADMIN** gère les comptes et consulte l'historique, mais **ne valide rien**.
- Le **MANAGER** demande, le **RH** vérifie, la **FINANCE** contrôle le budget,
  la **DIRECTION** décide seule des embauches.
- Chaque action est tracée dans la table `historique_action`.

## 7. Structure du projet

```
src/main/java/com/ecole/sirh/
  model/       entités JPA (16 tables)
  repository/  interfaces Spring Data
  service/     règles métier (circuit, transitions, intégration)
  web/         contrôleurs Thymeleaf
  security/    configuration Spring Security
  config/      SecurityConfig
src/main/resources/
  templates/   pages Thymeleaf
  application.properties
sql/
  01_schema.sql  schéma de la base
  02_data.sql    jeu de données de démonstration
```

## 8. Site public « carrières »

**http://localhost:4200/carrieres** est accessible sans connexion :
il liste les offres publiées et permet de postuler en ligne.
