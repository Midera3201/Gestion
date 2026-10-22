-- =====================================================================
--  SIRH - Etape 2 : creation des tables
--  Fichier : sql/01_schema.sql
--  Base    : PostgreSQL
--
--  Style simple :
--   - SERIAL PRIMARY KEY  -> id automatique (1, 2, 3...)
--   - CREATE TABLE IF NOT EXISTS -> pas d'erreur si la table existe
--   - DROP TABLE IF EXISTS au debut -> remise a zero propre
--
--  ATTENTION : le DROP supprime les tables ET les donnees.
--  A n'executer que pour (re)creer la base, jamais sur des donnees reelles.
-- =====================================================================

\set ON_ERROR_STOP on
\encoding UTF8

-- On supprime dans l'ordre INVERSE des dependances :
-- on ne peut pas supprimer "departement" avant "poste",
-- car "poste" contient une cle etrangere vers "departement".
DROP TABLE IF EXISTS historique_action;
DROP TABLE IF EXISTS tache_integration;
DROP TABLE IF EXISTS contrat;
DROP TABLE IF EXISTS employe;
DROP TABLE IF EXISTS decision_embauche;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS entretien;
DROP TABLE IF EXISTS candidature;
DROP TABLE IF EXISTS candidat;
DROP TABLE IF EXISTS offre_emploi;
DROP TABLE IF EXISTS validation;
DROP TABLE IF EXISTS demande_recrutement;
DROP TABLE IF EXISTS poste;
DROP TABLE IF EXISTS departement;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS role;


-- ---------------------------------------------------------------------
-- 1. ROLE : les 5 roles du site
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
    id_role     SERIAL PRIMARY KEY,
    code_role   TEXT NOT NULL UNIQUE,     -- ADMIN, MANAGER, RH, FINANCE, DIRECTION
    libelle_role TEXT NOT NULL
);


-- ---------------------------------------------------------------------
-- 2. UTILISATEUR : les comptes de connexion
--    id_role -> role.id_role : un role est partage par plusieurs personnes
--    mot_de_passe : hash BCrypt (jamais le mot de passe en clair)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur  SERIAL PRIMARY KEY,
    nom_utilisateur TEXT NOT NULL,
    prenom_utilisateur TEXT NOT NULL,
    email_utilisateur  TEXT NOT NULL UNIQUE,
    mot_de_passe    TEXT NOT NULL,
    actif           BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_role         INTEGER NOT NULL,
    CONSTRAINT fk_utilisateur_role
        FOREIGN KEY (id_role) REFERENCES role (id_role) ON DELETE RESTRICT
);


-- ---------------------------------------------------------------------
-- 3. DEPARTEMENT : les services de l'entreprise
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departement (
    id_departement     SERIAL PRIMARY KEY,
    nom_departement    TEXT NOT NULL UNIQUE,
    description_departement TEXT,
    budget_annuel      NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (budget_annuel >= 0)
);


-- ---------------------------------------------------------------------
-- 4. POSTE : les fonctions d'un departement
--    id_departement -> departement.id_departement
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS poste (
    id_poste            SERIAL PRIMARY KEY,
    titre_poste         TEXT NOT NULL,
    description_poste   TEXT,
    salaire_min         NUMERIC(12,2),
    salaire_max         NUMERIC(12,2),
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    id_departement      INTEGER NOT NULL,
    CONSTRAINT fk_poste_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement) ON DELETE RESTRICT,
    CONSTRAINT ck_poste_salaire
        CHECK (salaire_min IS NULL OR salaire_max IS NULL OR salaire_min <= salaire_max)
);


-- ---------------------------------------------------------------------
-- 5. DEMANDE_RECRUTEMENT : la demande creee par un MANAGER
--    id_poste       -> poste.id_poste
--    id_departement -> departement.id_departement
--    id_demandeur   -> utilisateur.id_utilisateur
--    statut_demande = etape courante du circuit
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS demande_recrutement (
    id_demande      SERIAL PRIMARY KEY,
    reference_demande TEXT NOT NULL UNIQUE,   -- ex: DEM-2026-001
    motif_demande   TEXT NOT NULL,
    nombre_postes   INTEGER NOT NULL DEFAULT 1 CHECK (nombre_postes > 0),
    budget_estime   NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (budget_estime >= 0),
    date_besoin     DATE NOT NULL,
    date_creation   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut_demande  TEXT NOT NULL DEFAULT 'EN_ATTENTE_RH',
    finance_requise BOOLEAN NOT NULL DEFAULT TRUE,
    id_poste        INTEGER NOT NULL,
    id_departement  INTEGER NOT NULL,
    id_demandeur    INTEGER NOT NULL,
    CONSTRAINT fk_demande_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste) ON DELETE RESTRICT,
    CONSTRAINT fk_demande_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement) ON DELETE RESTRICT,
    CONSTRAINT fk_demande_demandeur
        FOREIGN KEY (id_demandeur) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT,
    CONSTRAINT ck_demande_statut CHECK (statut_demande IN (
        'EN_ATTENTE_RH',
        'EN_ATTENTE_FINANCE',
        'EN_ATTENTE_DIRECTION',
        'ACCEPTEE',
        'REFUSEE',
        'ANNULEE'
    ))
);


-- ---------------------------------------------------------------------
-- 6. VALIDATION : historique des decisions sur une demande
--    id_demande  -> demande_recrutement.id_demande
--    id_valideur -> utilisateur.id_utilisateur (qui a signe ?)
--    On garde aussi les refus : c'est la tracabilite.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS validation (
    id_validation       SERIAL PRIMARY KEY,
    etape_validation    TEXT NOT NULL CHECK (etape_validation IN ('RH', 'FINANCE', 'DIRECTION')),
    decision_validation TEXT NOT NULL CHECK (decision_validation IN ('VALIDEE', 'REFUSEE')),
    commentaire_validation TEXT,
    date_decision       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_demande          INTEGER NOT NULL,
    id_valideur         INTEGER NOT NULL,
    CONSTRAINT fk_validation_demande
        FOREIGN KEY (id_demande) REFERENCES demande_recrutement (id_demande) ON DELETE CASCADE,
    CONSTRAINT fk_validation_valideur
        FOREIGN KEY (id_valideur) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT,
    CONSTRAINT uq_validation_demande_etape UNIQUE (id_demande, etape_validation)
);


-- ---------------------------------------------------------------------
-- 7. OFFRE_EMPLOI : l'annonce publiee apres acceptation
--    id_demande -> demande_recrutement.id_demande (UNIQUE = une offre max)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS offre_emploi (
    id_offre         SERIAL PRIMARY KEY,
    titre_offre      TEXT NOT NULL,
    description_offre TEXT NOT NULL,
    lieu_offre       TEXT,
    type_contrat     TEXT NOT NULL DEFAULT 'CDI'
        CHECK (type_contrat IN ('CDI', 'CDD', 'STAGE', 'ALTERNANCE')),
    date_publication TIMESTAMP,
    date_cloture     DATE,
    statut_offre     TEXT NOT NULL DEFAULT 'BROUILLON'
        CHECK (statut_offre IN ('BROUILLON', 'PUBLIEE', 'CLOTUREE')),
    id_demande       INTEGER NOT NULL UNIQUE,
    CONSTRAINT fk_offre_demande
        FOREIGN KEY (id_demande) REFERENCES demande_recrutement (id_demande) ON DELETE RESTRICT
);


-- ---------------------------------------------------------------------
-- 8. CANDIDAT : la personne qui postule
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidat (
    id_candidat         SERIAL PRIMARY KEY,
    nom_candidat        TEXT NOT NULL,
    prenom_candidat     TEXT NOT NULL,
    email_candidat      TEXT NOT NULL UNIQUE,
    telephone_candidat  TEXT,
    cv_candidat         TEXT,
    date_creation       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------------------
-- 9. CANDIDATURE : table de liaison N-N entre candidat et offre
--    id_candidat -> candidat.id_candidat
--    id_offre    -> offre_emploi.id_offre
--    UNIQUE(candidat, offre) : pas deux fois la meme candidature
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidature (
    id_candidature      SERIAL PRIMARY KEY,
    date_candidature    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut_candidature  TEXT NOT NULL DEFAULT 'RECUE'
        CHECK (statut_candidature IN (
            'RECUE',
            'EN_ENTRETIEN',
            'EVALUEE',
            'RETENUE',
            'REFUSEE',
            'OFFRE_ENVOYEE',
            'OFFRE_ACCEPTEE',
            'EMBAUCHEE'
        )),
    lettre_motivation   TEXT,
    id_candidat         INTEGER NOT NULL,
    id_offre            INTEGER NOT NULL,
    CONSTRAINT fk_candidature_candidat
        FOREIGN KEY (id_candidat) REFERENCES candidat (id_candidat) ON DELETE CASCADE,
    CONSTRAINT fk_candidature_offre
        FOREIGN KEY (id_offre) REFERENCES offre_emploi (id_offre) ON DELETE RESTRICT,
    CONSTRAINT uq_candidature UNIQUE (id_candidat, id_offre)
);


-- ---------------------------------------------------------------------
-- 10. ENTRETIEN : un rendez-vous pour une candidature
--     id_candidature -> candidature.id_candidature
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS entretien (
    id_entretien        SERIAL PRIMARY KEY,
    date_heure_entretien TIMESTAMP NOT NULL,
    lieu_entretien      TEXT,
    type_entretien      TEXT NOT NULL DEFAULT 'PRESENTIEL'
        CHECK (type_entretien IN ('PRESENTIEL', 'VISIO', 'TELEPHONE')),
    statut_entretien    TEXT NOT NULL DEFAULT 'PLANIFIE'
        CHECK (statut_entretien IN ('PLANIFIE', 'REALISE', 'ANNULE')),
    id_candidature      INTEGER NOT NULL,
    CONSTRAINT fk_entretien_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature (id_candidature) ON DELETE CASCADE
);


-- ---------------------------------------------------------------------
-- 11. EVALUATION : avis d'un evaluateur sur un entretien
--     id_entretien  -> entretien.id_entretien
--     id_evaluateur -> utilisateur.id_utilisateur
--     UNIQUE : une seule note par personne et par entretien
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evaluation (
    id_evaluation       SERIAL PRIMARY KEY,
    note_evaluation     INTEGER NOT NULL CHECK (note_evaluation BETWEEN 1 AND 5),
    points_forts        TEXT,
    points_faibles      TEXT,
    avis_evaluation     TEXT NOT NULL
        CHECK (avis_evaluation IN ('FAVORABLE', 'DEFAVORABLE', 'RESERVE')),
    date_evaluation     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_entretien        INTEGER NOT NULL,
    id_evaluateur       INTEGER NOT NULL,
    CONSTRAINT fk_evaluation_entretien
        FOREIGN KEY (id_entretien) REFERENCES entretien (id_entretien) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_evaluateur
        FOREIGN KEY (id_evaluateur) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT,
    CONSTRAINT uq_evaluation UNIQUE (id_entretien, id_evaluateur)
);


-- ---------------------------------------------------------------------
-- 12. DECISION_EMBAUCHE : accord final de la DIRECTION
--     id_candidature -> candidature.id_candidature (UNIQUE)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS decision_embauche (
    id_decision        SERIAL PRIMARY KEY,
    decision_finale    TEXT NOT NULL CHECK (decision_finale IN ('APPROUVEE', 'REFUSEE')),
    commentaire_decision TEXT,
    date_decision      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_candidature     INTEGER NOT NULL UNIQUE,
    id_decideur        INTEGER NOT NULL,
    CONSTRAINT fk_decision_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature (id_candidature) ON DELETE CASCADE,
    CONSTRAINT fk_decision_decideur
        FOREIGN KEY (id_decideur) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT
);


-- ---------------------------------------------------------------------
-- 13. EMPLOYE : dossier du salarie cree apres acceptation de l'offre
--     id_candidature -> candidature.id_candidature (d'ou vient l'employe)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employe (
    id_employe      SERIAL PRIMARY KEY,
    matricule       TEXT NOT NULL UNIQUE,       -- ex: EMP-2026-001
    nom_employe     TEXT NOT NULL,
    prenom_employe  TEXT NOT NULL,
    email_employe   TEXT NOT NULL,
    date_embauche   DATE NOT NULL,
    statut_employe  TEXT NOT NULL DEFAULT 'ACTIF'
        CHECK (statut_employe IN ('ACTIF', 'INACTIF')),
    date_creation   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_poste        INTEGER NOT NULL,
    id_departement  INTEGER NOT NULL,
    id_candidature  INTEGER UNIQUE,
    CONSTRAINT fk_employe_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste) ON DELETE RESTRICT,
    CONSTRAINT fk_employe_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement) ON DELETE RESTRICT,
    CONSTRAINT fk_employe_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature (id_candidature) ON DELETE SET NULL
);


-- ---------------------------------------------------------------------
-- 14. CONTRAT : contrat de travail d'un employe
--     id_employe -> employe.id_employe
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contrat (
    id_contrat      SERIAL PRIMARY KEY,
    type_contrat    TEXT NOT NULL CHECK (type_contrat IN ('CDI', 'CDD', 'STAGE', 'ALTERNANCE')),
    date_debut      DATE NOT NULL,
    date_fin        DATE,
    salaire         NUMERIC(12,2) NOT NULL CHECK (salaire > 0),
    statut_contrat  TEXT NOT NULL DEFAULT 'BROUILLON'
        CHECK (statut_contrat IN ('BROUILLON', 'SIGNE', 'TERMINE')),
    date_signature  DATE,
    id_employe      INTEGER NOT NULL,
    CONSTRAINT fk_contrat_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe) ON DELETE CASCADE,
    CONSTRAINT ck_contrat_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);


-- ---------------------------------------------------------------------
-- 15. TACHE_INTEGRATION : onboarding du nouvel employe
--     id_employe     -> employe.id_employe
--     id_responsable -> utilisateur.id_utilisateur
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tache_integration (
    id_tache          SERIAL PRIMARY KEY,
    libelle_tache     TEXT NOT NULL,
    date_prevue       DATE,
    date_realisation  DATE,
    statut_tache      TEXT NOT NULL DEFAULT 'A_FAIRE'
        CHECK (statut_tache IN ('A_FAIRE', 'EN_COURS', 'TERMINEE')),
    id_employe        INTEGER NOT NULL,
    id_responsable    INTEGER NOT NULL,
    CONSTRAINT fk_tache_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe) ON DELETE CASCADE,
    CONSTRAINT fk_tache_responsable
        FOREIGN KEY (id_responsable) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 16. HISTORIQUE_ACTION : journal des actions (lu par l'ADMIN)
--     id_utilisateur -> utilisateur.id_utilisateur
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historique_action (
    id_historique   SERIAL PRIMARY KEY,
    action_effectuee TEXT NOT NULL,
    entite_concernee TEXT NOT NULL,
    id_entite       INTEGER,
    details         TEXT,
    date_action     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur  INTEGER NOT NULL,
    CONSTRAINT fk_historique_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur) ON DELETE RESTRICT
);
