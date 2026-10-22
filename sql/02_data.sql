-- =====================================================================
--  SIRH - Etape 2 : donnees de test
--  Fichier : sql/02_data.sql
--  A executer APRES 01_schema.sql, sur la base sirh_db.
--
--  Mot de passe de TOUS les comptes de test : midera2026
--  (stocke en hash BCrypt, jamais en clair)
--
--  IMPORTANT - comment les cles etrangeres sont remplies :
--  TRUNCATE ... RESTART IDENTITY remet les compteurs a 1.
--  Les 5 roles sont donc crees avec id_role = 1, 2, 3, 4, 5,
--  les 7 utilisateurs avec id_utilisateur = 1 a 7, etc.
--  C'est pour cela qu'on peut ecrire id_role = 3 sans le deviner :
--  l'ordre des INSERT ci-dessous est volontaire.
--  Relancer ce fichier ne cree pas de doublons : il vide d'abord les tables.
-- =====================================================================

\set ON_ERROR_STOP on
\encoding UTF8  

TRUNCATE TABLE historique_action, tache_integration, contrat, employe,
               decision_embauche, evaluation, entretien, candidature,
               candidat, offre_emploi, validation, demande_recrutement,
               poste, departement, utilisateur, role
    RESTART IDENTITY CASCADE;


-- ---------------------------------------------------------------------
-- ROLES  (id_role : 1=ADMIN, 2=MANAGER, 3=RH, 4=FINANCE, 5=DIRECTION)
-- ---------------------------------------------------------------------
INSERT INTO role (code_role, libelle_role) VALUES
('ADMIN',     'Administrateur'),
('MANAGER',   'Manager'),
('RH',        'Ressources Humaines'),
('FINANCE',   'Finance'),
('DIRECTION', 'Direction');


-- ---------------------------------------------------------------------
-- UTILISATEURS
-- hash BCrypt du mot de passe "midera2026" (cout 10)
-- ---------------------------------------------------------------------
INSERT INTO utilisateur (nom_utilisateur, prenom_utilisateur, email_utilisateur, mot_de_passe, actif, id_role, date_creation) VALUES
('Dupont',  'Alice',  'admin@ecole.fr',     '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE,  1, '2026-01-05 09:00:00'),
('Martin',  'Karim',  'manager@ecole.fr',   '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE,  2, '2026-01-05 09:05:00'),
('Bernard', 'Sophie', 'rh@ecole.fr',        '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE,  3, '2026-01-05 09:10:00'),
('Petit',   'Julien', 'finance@ecole.fr',   '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE,  4, '2026-01-05 09:15:00'),
('Moreau',  'Claire', 'direction@ecole.fr', '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE,  5, '2026-01-05 09:20:00'),
('Rousseau','Emma',   'manager.marketing@ecole.fr', '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', TRUE, 2, '2026-01-06 10:00:00'),
('Lefevre', 'Thomas', 'manager.archive@ecole.fr',   '$2a$10$tFfpUJQQiQE1.EkcdLinzuFENNnt4I2uHF7n10YWpkvY7yEPNVRau', FALSE, 2, '2026-01-06 10:05:00');


-- ---------------------------------------------------------------------
-- DEPARTEMENTS  (id_departement : 1 a 5)
-- ---------------------------------------------------------------------
INSERT INTO departement (nom_departement, description_departement, budget_annuel) VALUES
('Direction Generale',  'Direction de l entreprise',                500000.00),
('Ressources Humaines', 'Recrutement, paie et integration',         250000.00),
('Informatique',        'Developpement et infrastructure',          800000.00),
('Finance',             'Comptabilite et controle de gestion',      300000.00),
('Marketing',           'Communication et developpement commercial',150000.00);


-- ---------------------------------------------------------------------
-- POSTES  (id_poste : 1 a 6)
-- ---------------------------------------------------------------------
INSERT INTO poste (titre_poste, description_poste, salaire_min, salaire_max, actif, id_departement) VALUES
('Developpeur Java',        'Developpement des applications web internes',   38000, 48000, TRUE, 3),
('Chef de projet SI',       'Pilotage des projets informatiques',            45000, 60000, TRUE, 3),
('Charge de recrutement',   'Sourcing, entretiens et suivi des candidats',   30000, 38000, TRUE, 2),
('Comptable',               'Comptabilite generale et fournisseurs',         32000, 42000, TRUE, 4),
('Responsable marketing',   'Animation de la marque et campagnes digitales', 40000, 52000, TRUE, 5),
('Assistant administratif', 'Support administratif du service RH',           26000, 32000, TRUE, 2);


-- ---------------------------------------------------------------------
-- DEMANDES  (id_demande : 1 a 5)
--   1 : en attente DIRECTION (RH + FINANCE ont valide)
--   2 : en attente RH (debut du circuit)
--   3 : acceptee, offre cloturee, candidat embauche
--   4 : refusee (budget)
--   5 : acceptee sans passage par la FINANCE (finance_requise = FALSE)
-- ---------------------------------------------------------------------
INSERT INTO demande_recrutement
    (reference_demande, motif_demande, nombre_postes, budget_estime, date_besoin,
     date_creation, statut_demande, finance_requise, id_poste, id_departement, id_demandeur) VALUES
('DEM-2026-001', 'Renfort de l equipe web pour le nouveau portail client.',      2, 90000.00, '2026-12-01', '2026-09-15 09:30:00', 'EN_ATTENTE_DIRECTION', TRUE,  1, 3, 2),
('DEM-2026-002', 'Besoin d un charge de recrutement pour absorber les volumes.', 1, 36000.00, '2027-01-05', '2026-10-12 14:10:00', 'EN_ATTENTE_RH',        TRUE,  3, 2, 2),
('DEM-2026-003', 'Creation du poste de responsable marketing digital.',          1, 50000.00, '2026-11-02', '2026-08-10 11:00:00', 'ACCEPTEE',             TRUE,  5, 5, 6),
('DEM-2026-004', 'Second developpeur Java pour l equipe projet.',                1, 45000.00, '2026-10-01', '2026-07-01 10:15:00', 'REFUSEE',              TRUE,  1, 3, 2),
('DEM-2026-005', 'Remplacement du chef de projet partant en decembre.',          1, 55000.00, '2026-12-15', '2026-08-28 08:45:00', 'ACCEPTEE',             FALSE, 2, 3, 2);


-- ---------------------------------------------------------------------
-- VALIDATIONS  (id_validation : 1 a 10)
-- 1 a 2   : demande 1 (RH puis FINANCE)
-- 3 a 5   : demande 3 (RH, FINANCE, DIRECTION)
-- 6 a 8   : demande 4 (refus finance puis direction)
-- 9 a 10  : demande 5 (RH puis DIRECTION, pas de finance)
-- ---------------------------------------------------------------------
INSERT INTO validation
    (etape_validation, decision_validation, commentaire_validation, date_decision, id_demande, id_valideur) VALUES
('RH',        'VALIDEE', 'Fiche de poste et besoin conformes.',                              '2026-09-16 10:00:00', 1, 3),
('FINANCE',   'VALIDEE', 'Budget 2026 disponible pour 2 postes.',                            '2026-09-18 15:20:00', 1, 4),
('RH',        'VALIDEE', 'Demande complete, transmission a la direction.',                   '2026-08-11 09:10:00', 3, 3),
('FINANCE',   'VALIDEE', 'Enveloppe marketing suffisante.',                                  '2026-08-12 16:00:00', 3, 4),
('DIRECTION', 'VALIDEE', 'Poste strategique, recrutement autorise.',                         '2026-08-14 11:30:00', 3, 5),
('RH',        'VALIDEE', 'Besoin confirme par le manager.',                                  '2026-07-02 14:00:00', 4, 3),
('FINANCE',   'REFUSEE', 'Budget 2026 deja engage a 92 pourcent.',                           '2026-07-04 09:45:00', 4, 4),
('DIRECTION', 'REFUSEE', 'Demande close : arbitrage budgetaire.',                            '2026-07-06 17:00:00', 4, 5),
('RH',        'VALIDEE', 'Remplacement justifie, passation prevue en decembre.',             '2026-09-02 08:55:00', 5, 3),
('DIRECTION', 'VALIDEE', 'Remplacement valide, pas de controle budgetaire supplementaire.',  '2026-09-04 10:05:00', 5, 5);


-- ---------------------------------------------------------------------
-- OFFRES  (id_offre : 1 et 2)
-- ---------------------------------------------------------------------
INSERT INTO offre_emploi
    (titre_offre, description_offre, lieu_offre, type_contrat,
     date_publication, date_cloture, statut_offre, id_demande) VALUES
('Responsable marketing digital',
 'Vous pilotez la presence en ligne de l entreprise : site, reseaux sociaux et campagnes. Vous managez une equipe de deux personnes.',
 'Paris', 'CDI', '2026-08-18 09:00:00', '2026-09-30', 'CLOTUREE', 3),
('Chef de projet SI',
 'Vous pilotez le remplacement du systeme d information RH : planning, budget, coordination des prestataires et des equipes internes.',
 'Lyon', 'CDI', '2026-09-08 09:30:00', '2026-11-15', 'PUBLIEE', 5);


-- ---------------------------------------------------------------------
-- CANDIDATS  (id_candidat : 1 a 5)
-- ---------------------------------------------------------------------
INSERT INTO candidat (nom_candidat, prenom_candidat, email_candidat, telephone_candidat, cv_candidat, date_creation) VALUES
('Nguyen', 'Linh',   'linh.nguyen@mail.com',   '0601020304', 'cv/linh-nguyen.pdf',   '2026-08-19 10:00:00'),
('Diallo', 'Amadou', 'amadou.diallo@mail.com', '0605060708', 'cv/amadou-diallo.pdf', '2026-08-21 11:30:00'),
('Rossi',  'Marco',  'marco.rossi@mail.com',   '0609101112', 'cv/marco-rossi.pdf',   '2026-09-12 15:45:00'),
('Traore', 'Fatou',  'fatou.traore@mail.com',  '0613141516', 'cv/fatou-traore.pdf',  '2026-09-15 09:20:00'),
('Leroy',  'Hugo',   'hugo.leroy@mail.com',    '0617181920', 'cv/hugo-leroy.pdf',    '2026-09-20 18:10:00');


-- ---------------------------------------------------------------------
-- CANDIDATURES  (id_candidature : 1 a 5)
-- offre 1 : Linh embauchee, Amadou refuse
-- offre 2 : Marco recu, Fatou et Hugo en entretien
-- ---------------------------------------------------------------------
INSERT INTO candidature (date_candidature, statut_candidature, lettre_motivation, id_candidat, id_offre) VALUES
('2026-08-20 09:15:00', 'EMBAUCHEE',    'Je souhaite structurer la presence digitale de l entreprise.', 1, 1),
('2026-08-22 14:00:00', 'REFUSEE',      'Disponible immediatement, forte experience evenementielle.',   2, 1),
('2026-09-12 15:45:00', 'RECUE',        'Je pilote des projets SI depuis six ans.',                    3, 2),
('2026-09-15 09:20:00', 'EN_ENTRETIEN', 'Experience en conduite du changement et en gestion RH.',      4, 2),
('2026-09-20 18:10:00', 'EN_ENTRETIEN', 'Je souhaite evoluer vers le pilotage de projet.',             5, 2);


-- ---------------------------------------------------------------------
-- ENTRETIENS  (id_entretien : 1 a 4)
-- ---------------------------------------------------------------------
INSERT INTO entretien (date_heure_entretien, lieu_entretien, type_entretien, statut_entretien, id_candidature) VALUES
('2026-08-28 10:00:00', 'Visio Teams',         'VISIO',      'REALISE',  1),
('2026-08-29 11:00:00', 'Salle RH - 2e etage', 'PRESENTIEL', 'REALISE',  2),
('2026-10-28 14:00:00', 'Visio Teams',         'VISIO',      'PLANIFIE', 4),
('2026-10-29 09:30:00', 'Salle RH - 2e etage', 'PRESENTIEL', 'PLANIFIE', 5);


-- ---------------------------------------------------------------------
-- EVALUATIONS  (id_evaluation : 1 a 4)
-- deux avis par entretien : le RH (id 3) et le manager (id 6)
-- ---------------------------------------------------------------------
INSERT INTO evaluation
    (note_evaluation, points_forts, points_faibles, avis_evaluation,
     date_evaluation, id_entretien, id_evaluateur) VALUES
(4, 'Tres bonne vision du digital, autonomie.',       'Manque d experience en management direct.', 'FAVORABLE',   '2026-08-28 12:00:00', 1, 3),
(5, 'Excellent portfolio, tres bonne preparation.',   'Aucun point bloquant identifie.',           'FAVORABLE',   '2026-08-28 18:00:00', 1, 6),
(3, 'Experience evenementielle interessante.',        'Trop eloigne du poste de responsable.',     'DEFAVORABLE', '2026-08-29 12:00:00', 2, 3),
(2, 'Motivation correcte.',                           'Pas les competences digitales attendues.',  'DEFAVORABLE', '2026-08-29 18:00:00', 2, 6);


-- ---------------------------------------------------------------------
-- DECISION D EMBAUCHE (accord final de la DIRECTION)
-- ---------------------------------------------------------------------
INSERT INTO decision_embauche
    (decision_finale, commentaire_decision, date_decision, id_candidature, id_decideur) VALUES
('APPROUVEE', 'Proposition conforme aux evaluations, salaire dans la fourchette du poste.', '2026-09-05 10:30:00', 1, 5);


-- ---------------------------------------------------------------------
-- EMPLOYE (cree apres acceptation de l offre)
-- ---------------------------------------------------------------------
INSERT INTO employe
    (matricule, nom_employe, prenom_employe, email_employe, date_embauche,
     statut_employe, date_creation, id_poste, id_departement, id_candidature) VALUES
('EMP-2026-001', 'Nguyen', 'Linh', 'linh.nguyen@ecole.fr', '2026-10-01',
 'ACTIF', '2026-09-20 09:00:00', 5, 5, 1);


-- ---------------------------------------------------------------------
-- CONTRAT
-- ---------------------------------------------------------------------
INSERT INTO contrat
    (type_contrat, date_debut, date_fin, salaire, statut_contrat, date_signature, id_employe) VALUES
('CDI', '2026-10-01', NULL, 46000.00, 'SIGNE', '2026-09-20', 1);


-- ---------------------------------------------------------------------
-- TACHES D INTEGRATION
-- ---------------------------------------------------------------------
INSERT INTO tache_integration
    (libelle_tache, date_prevue, date_realisation, statut_tache, id_employe, id_responsable) VALUES
('Signature du contrat de travail',     '2026-09-20', '2026-09-20', 'TERMINEE', 1, 3),
('Remise du materiel informatique',     '2026-10-01', '2026-10-01', 'TERMINEE', 1, 3),
('Formation securite et RGPD',          '2026-10-05', NULL,         'EN_COURS', 1, 3),
('Rencontre avec l equipe marketing',   '2026-10-02', '2026-10-02', 'TERMINEE', 1, 6),
('Definition des objectifs a 90 jours', '2026-12-30', NULL,         'A_FAIRE',  1, 6);


-- ---------------------------------------------------------------------
-- HISTORIQUE DES ACTIONS
-- ---------------------------------------------------------------------
INSERT INTO historique_action (action_effectuee, entite_concernee, id_entite, details, date_action, id_utilisateur) VALUES
('CONNEXION',                'utilisateur',         1, 'Connexion reussie sur le tableau de bord.',    '2026-09-15 09:25:00', 1),
('CREATION_DEMANDE',         'demande_recrutement', 1, 'Demande DEM-2026-001 creee.',                  '2026-09-15 09:30:00', 2),
('VALIDATION_DEMANDE',       'demande_recrutement', 1, 'Etape RH validee.',                            '2026-09-16 10:00:00', 3),
('VALIDATION_DEMANDE',       'demande_recrutement', 3, 'Decision finale : demande acceptee.',          '2026-08-14 11:30:00', 5),
('PUBLICATION_OFFRE',        'offre_emploi',        2, 'Offre Chef de projet SI publiee.',             '2026-09-08 09:30:00', 3),
('CREATION_EMPLOYE',         'employe',             1, 'Dossier EMP-2026-001 cree apres acceptation.', '2026-09-20 09:00:00', 3),
('DESACTIVATION_UTILISATEUR','utilisateur',         7, 'Compte de Thomas Lefevre desactive.',          '2026-06-30 16:00:00', 1);


-- ---------------------------------------------------------------------
-- Verification rapide : les 16 tables doivent contenir des lignes.
-- ---------------------------------------------------------------------
SELECT 'role' AS table_name, COUNT(*) AS nb_lignes FROM role
UNION ALL SELECT 'utilisateur',         COUNT(*) FROM utilisateur
UNION ALL SELECT 'departement',         COUNT(*) FROM departement
UNION ALL SELECT 'poste',               COUNT(*) FROM poste
UNION ALL SELECT 'demande_recrutement', COUNT(*) FROM demande_recrutement
UNION ALL SELECT 'validation',          COUNT(*) FROM validation
UNION ALL SELECT 'offre_emploi',        COUNT(*) FROM offre_emploi
UNION ALL SELECT 'candidat',            COUNT(*) FROM candidat
UNION ALL SELECT 'candidature',         COUNT(*) FROM candidature
UNION ALL SELECT 'entretien',           COUNT(*) FROM entretien
UNION ALL SELECT 'evaluation',          COUNT(*) FROM evaluation
UNION ALL SELECT 'decision_embauche',   COUNT(*) FROM decision_embauche
UNION ALL SELECT 'employe',             COUNT(*) FROM employe
UNION ALL SELECT 'contrat',             COUNT(*) FROM contrat
UNION ALL SELECT 'tache_integration',   COUNT(*) FROM tache_integration
UNION ALL SELECT 'historique_action',   COUNT(*) FROM historique_action
ORDER BY table_name;
