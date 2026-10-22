-- =====================================================================
--  SIRH - Etape 2 : verifications
--  Fichier : sql/03_verifications.sql
--  A executer quand tu veux :  psql -U postgres -h localhost -d sirh_db -f .\sql\03_verifications.sql
--
--  Ce fichier ne modifie RIEN. Il ne supprime rien et n'insere rien.
--  Tu peux donc le lancer autant de fois que tu veux, meme apres avoir
--  saisi de vraies donnees dans le site : rien ne sera perdu.
-- =====================================================================

\encoding UTF8
\pset pager off

\echo '=== 1. Nombre de lignes par table ==='
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


\echo '=== 2. Comptes utilisateurs et roles ==='
SELECT u.id_utilisateur,
       u.prenom_utilisateur || ' ' || u.nom_utilisateur AS nom_complet,
       u.email_utilisateur,
       r.code_role,
       u.actif
FROM utilisateur u
JOIN role r ON r.id_role = u.id_role
ORDER BY u.id_utilisateur;


\echo '=== 3. Demandes de recrutement (poste, departement, demandeur) ==='
SELECT d.reference_demande,
       p.titre_poste        AS poste,
       dep.nom_departement  AS departement,
       u.prenom_utilisateur || ' ' || u.nom_utilisateur AS demandeur,
       d.nombre_postes,
       d.budget_estime,
       d.statut_demande,
       d.finance_requise
FROM demande_recrutement d
JOIN poste p         ON p.id_poste = d.id_poste
JOIN departement dep ON dep.id_departement = d.id_departement
JOIN utilisateur u   ON u.id_utilisateur = d.id_demandeur
ORDER BY d.id_demande;


\echo '=== 4. Qui a valide quoi, quand, et pourquoi ==='
SELECT d.reference_demande,
       v.etape_validation,
       v.decision_validation,
       u.prenom_utilisateur || ' ' || u.nom_utilisateur AS valideur,
       v.date_decision,
       v.commentaire_validation
FROM validation v
JOIN demande_recrutement d ON d.id_demande = v.id_demande
JOIN utilisateur u         ON u.id_utilisateur = v.id_valideur
ORDER BY d.id_demande, v.date_decision;


\echo '=== 5. Circuit complet : demande -> offre -> candidature -> embauche ==='
SELECT d.reference_demande,
       o.titre_offre,
       c.prenom_candidat || ' ' || c.nom_candidat AS candidat,
       ca.statut_candidature,
       de.decision_finale,
       e.matricule,
       ct.type_contrat,
       ct.salaire
FROM demande_recrutement d
JOIN offre_emploi o            ON o.id_demande = d.id_demande
JOIN candidature ca            ON ca.id_offre = o.id_offre
JOIN candidat c                ON c.id_candidat = ca.id_candidat
LEFT JOIN decision_embauche de ON de.id_candidature = ca.id_candidature
LEFT JOIN employe e            ON e.id_candidature = ca.id_candidature
LEFT JOIN contrat ct           ON ct.id_employe = e.id_employe
ORDER BY d.id_demande, ca.id_candidature;


\echo '=== 6. Regle FINANCE : un avis finance est exige seulement si finance_requise = TRUE ==='
SELECT d.reference_demande,
       d.statut_demande,
       d.finance_requise,
       CASE
           WHEN d.finance_requise = FALSE THEN 'Non applicable'
           WHEN v.id_validation IS NULL   THEN 'MANQUANT : a verifier'
           ELSE v.decision_validation
       END AS avis_finance
FROM demande_recrutement d
LEFT JOIN validation v
       ON v.id_demande = d.id_demande AND v.etape_validation = 'FINANCE'
WHERE d.statut_demande IN ('EN_ATTENTE_DIRECTION', 'ACCEPTEE')
ORDER BY d.id_demande;


\echo '=== 7. Entretiens, evaluations et note moyenne ==='
SELECT c.prenom_candidat || ' ' || c.nom_candidat AS candidat,
       o.titre_offre,
       en.date_heure_entretien,
       en.statut_entretien,
       ROUND(AVG(ev.note_evaluation), 2) AS note_moyenne,
       COUNT(ev.id_evaluation)           AS nb_evaluations
FROM entretien en
JOIN candidature ca ON ca.id_candidature = en.id_candidature
JOIN candidat c     ON c.id_candidat = ca.id_candidat
JOIN offre_emploi o ON o.id_offre = ca.id_offre
LEFT JOIN evaluation ev ON ev.id_entretien = en.id_entretien
GROUP BY c.id_candidat, c.prenom_candidat, c.nom_candidat,
         o.titre_offre, en.id_entretien, en.date_heure_entretien, en.statut_entretien
ORDER BY en.date_heure_entretien;


\echo '=== 8. Employes, contrats et taches d integration ==='
SELECT e.matricule,
       e.prenom_employe || ' ' || e.nom_employe AS employe,
       p.titre_poste    AS poste,
       dep.nom_departement AS departement,
       e.date_embauche,
       ct.type_contrat,
       ct.salaire,
       ct.statut_contrat,
       COUNT(t.id_tache) FILTER (WHERE t.statut_tache = 'TERMINEE') AS taches_terminees,
       COUNT(t.id_tache)                                            AS taches_total
FROM employe e
JOIN poste p          ON p.id_poste = e.id_poste
JOIN departement dep  ON dep.id_departement = e.id_departement
LEFT JOIN contrat ct  ON ct.id_employe = e.id_employe
LEFT JOIN tache_integration t ON t.id_employe = e.id_employe
GROUP BY e.id_employe, e.matricule, e.prenom_employe, e.nom_employe,
         p.titre_poste, dep.nom_departement, e.date_embauche,
         ct.type_contrat, ct.salaire, ct.statut_contrat;


\echo '=== 9. Integrite : lignes orphelines (0 attendu partout) ==='
SELECT 'poste sans departement' AS controle, COUNT(*) AS problemes
FROM poste p LEFT JOIN departement d ON d.id_departement = p.id_departement
WHERE d.id_departement IS NULL
UNION ALL
SELECT 'demande sans poste', COUNT(*)
FROM demande_recrutement dr LEFT JOIN poste p ON p.id_poste = dr.id_poste
WHERE p.id_poste IS NULL
UNION ALL
SELECT 'validation sans valideur', COUNT(*)
FROM validation v LEFT JOIN utilisateur u ON u.id_utilisateur = v.id_valideur
WHERE u.id_utilisateur IS NULL
UNION ALL
SELECT 'offre sans demande', COUNT(*)
FROM offre_emploi o LEFT JOIN demande_recrutement d ON d.id_demande = o.id_demande
WHERE d.id_demande IS NULL
UNION ALL
SELECT 'candidature sans candidat', COUNT(*)
FROM candidature c LEFT JOIN candidat ca ON ca.id_candidat = c.id_candidat
WHERE ca.id_candidat IS NULL
UNION ALL
SELECT 'entretien sans candidature', COUNT(*)
FROM entretien e LEFT JOIN candidature c ON c.id_candidature = e.id_candidature
WHERE c.id_candidature IS NULL
UNION ALL
SELECT 'employe sans poste', COUNT(*)
FROM employe e LEFT JOIN poste p ON p.id_poste = e.id_poste
WHERE p.id_poste IS NULL;


\echo '=== 10. Les mots de passe sont bien hashes (60 caracteres, $2a$10$) ==='
SELECT email_utilisateur,
       LEFT(mot_de_passe, 7) || '...' AS debut_du_hash,
       LENGTH(mot_de_passe) AS longueur
FROM utilisateur
ORDER BY id_utilisateur;


\echo '=== 11. Derniere date enregistree (preuve que les donnees sont bien la) ==='
SELECT 'utilisateur' AS table_name, MAX(date_creation) AS derniere_date FROM utilisateur
UNION ALL SELECT 'demande_recrutement', MAX(date_creation)  FROM demande_recrutement
UNION ALL SELECT 'validation',          MAX(date_decision)  FROM validation
UNION ALL SELECT 'candidature',         MAX(date_candidature) FROM candidature
UNION ALL SELECT 'historique_action',   MAX(date_action)    FROM historique_action;
