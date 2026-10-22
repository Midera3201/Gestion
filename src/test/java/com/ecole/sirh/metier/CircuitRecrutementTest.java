package com.ecole.sirh.metier;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.Contrat;
import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.Departement;
import com.ecole.sirh.model.Employe;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.model.Poste;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.EntretienRepository;
import com.ecole.sirh.repository.EvaluationRepository;
import com.ecole.sirh.repository.OffreEmploiRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.service.CandidatureService;
import com.ecole.sirh.service.DemandeService;
import com.ecole.sirh.service.DecisionService;
import com.ecole.sirh.service.EmployeService;
import com.ecole.sirh.service.OffreService;
import com.ecole.sirh.service.RegleMetierException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ETAPE 11 : les tests du circuit de recrutement.
 *
 * Ce test verifie les REGLES DE GESTION, pas seulement le code :
 *  - une demande passe bien par RH -> FINANCE -> DIRECTION
 *  - un refus arrete le circuit
 *  - on ne peut pas sauter une etape
 *  - une etape ne se signe qu'une fois
 *  - une offre nait seulement d'une demande acceptee
 *  - une candidature ne peut pas "revenir en arriere"
 *  - un dossier employe nait seulement d'une offre acceptee
 *
 * On utilise la VRAIE base PostgreSQL (comme dans le reste du projet).
 * @Transactional : chaque test est annule en fin d'execution, donc la
 * base de test ne pollue jamais les donnees de 02_data.sql.
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.main.banner-mode=off")
class CircuitRecrutementTest {

    @Autowired private DemandeService demandeService;
    @Autowired private OffreService offreService;
    @Autowired private CandidatureService candidatureService;
    @Autowired private DecisionService decisionService;
    @Autowired private EmployeService employeService;

    @Autowired private DemandeRecrutementRepository demandeRepository;
    @Autowired private PosteRepository posteRepository;
    @Autowired private DepartementRepository departementRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private OffreEmploiRepository offreRepository;
    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private EntretienRepository entretienRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private com.ecole.sirh.repository.TacheIntegrationRepository tacheIntegrationRepository;

    // Les comptes utiles (voir 02_data.sql).
    private static final long ID_MANAGER = 2;
    private static final long ID_RH = 3;
    private static final long ID_FINANCE = 4;
    private static final long ID_DIRECTION = 5;

    // ------------------------------------------------------------------
    // 1. LE CIRCUIT DES DEMANDES
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Une nouvelle demande commence toujours par l'etape RH")
    void uneNouvelleDemandeCommenceParLeRh() {

        DemandeRecrutement demande = creerDemande(true);

        assertEquals("EN_ATTENTE_RH", demande.getStatutDemande());
        assertEquals("RH", demandeService.etapeAttendue(demande));
        assertTrue(demande.getReferenceDemande().startsWith("DEM-"));
    }

    @Test
    @DisplayName("La validation RH fait passer la demande a l'etape FINANCE")
    void laValidationRhFaitAvancerLeCircuit() {

        DemandeRecrutement demande = creerDemande(true);

        demandeService.valider(demande, "RH", true, "Demande complete.", rh());

        assertEquals("EN_ATTENTE_FINANCE", demande.getStatutDemande());
        assertEquals("FINANCE", demandeService.etapeAttendue(demande));
    }

    @Test
    @DisplayName("La FINANCE puis la DIRECTION terminent le circuit : demande acceptee")
    void leCircuitSeTermineParLaDirection() {

        DemandeRecrutement demande = creerDemande(true);

        demandeService.valider(demande, "RH", true, "", rh());
        demandeService.valider(demande, "FINANCE", true, "Budget disponible.", finance());
        demandeService.valider(demande, "DIRECTION", true, "Accord.", direction());

        assertEquals("ACCEPTEE", demande.getStatutDemande());
        // La demande n'est plus dans le circuit : plus personne ne doit signer.
        assertEquals(null, demandeService.etapeAttendue(demande));
    }

    @Test
    @DisplayName("Une demande sans controle budgetaire saute l'etape FINANCE")
    void uneDemandeSansBudgetSaupeLaFinance() {

        DemandeRecrutement demande = creerDemande(false);

        demandeService.valider(demande, "RH", true, "", rh());

        assertEquals("EN_ATTENTE_DIRECTION", demande.getStatutDemande());
    }

    @Test
    @DisplayName("Un refus arrete immediatement le circuit")
    void unRefusArreteLeCircuit() {

        DemandeRecrutement demande = creerDemande(true);

        demandeService.valider(demande, "RH", false, "Besoin non justifie.", rh());

        assertEquals("REFUSEE", demande.getStatutDemande());
        // Apres un refus, plus aucune etape n'est attendue.
        assertEquals(null, demandeService.etapeAttendue(demande));
    }

    @Test
    @DisplayName("On ne peut pas sauter une etape : la FINANCE avant le RH est refusee")
    void onNePeutPasSauterUneEtape() {

        DemandeRecrutement demande = creerDemande(true);

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> demandeService.valider(demande, "FINANCE", true, "", finance()));

        assertTrue(e.getMessage().contains("tour"));
        // Le statut n'a pas bouge.
        assertEquals("EN_ATTENTE_RH", demande.getStatutDemande());
    }

    @Test
    @DisplayName("Une etape deja signee ne peut pas l'etre une deuxieme fois")
    void uneEtapeNeSeSigneQuUneFois() {

        DemandeRecrutement demande = creerDemande(false);
        demandeService.valider(demande, "RH", true, "", rh());

        assertThrows(RegleMetierException.class,
                () -> demandeService.valider(demande, "RH", true, "", rh()));
    }

    @Test
    @DisplayName("Une demande terminee ne peut plus etre annulee")
    void uneDemandeTermineeNeSePlusAnnulee() {

        DemandeRecrutement demande = creerDemande(false);
        demandeService.valider(demande, "RH", true, "", rh());
        demandeService.valider(demande, "DIRECTION", true, "", direction());

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> demandeService.annuler(demande, manager()));
        assertTrue(e.getMessage().contains("annulable"));
    }

    @Test
    @DisplayName("Une demande creee avec un budget negatif est refusee")
    void uneDemandeAvecBudgetNegatifEstRefusee() {

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> demandeService.creer(poste(1L), departement(1L), manager(),
                        "Test budget", 1, new BigDecimal("-100"), LocalDate.now(), true));
        assertTrue(e.getMessage().contains("budget"));
    }

    // ------------------------------------------------------------------
    // 2. LES OFFRES
    // ------------------------------------------------------------------

    @Test
    @DisplayName("On ne peut pas creer une offre a partir d'une demande non acceptee")
    void pasDOffreSansDemandeAcceptee() {

        // La demande 1 attend encore la Direction.
        DemandeRecrutement demande = demandeRepository.findById(1L).orElseThrow();

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> offreService.creer(demande, "Titre", "Description", "Paris",
                        "CDI", LocalDate.now(), rh()));

        assertTrue(e.getMessage().contains("acceptee"));
    }

    @Test
    @DisplayName("Une offre naissante est en brouillon, puis publiee")
    void uneOffreNaissanteEstUnBrouillon() {

        DemandeRecrutement demande = creerDemande(false);
        demandeService.valider(demande, "RH", true, "", rh());
        demandeService.valider(demande, "DIRECTION", true, "", direction());

        OffreEmploi offre = offreService.creer(demande, "Developpeur Java",
                "Poste a pourvoir rapidement.", "Paris", "CDI", null, rh());

        assertEquals("BROUILLON", offre.getStatutOffre());
        // Une seule offre par demande (contrainte UNIQUE en base).
        assertTrue(offreRepository.findByDemande_IdDemande(demande.getIdDemande()).isPresent());

        offreService.publier(offre, LocalDate.now().plusMonths(1), rh());
        assertEquals("PUBLIEE", offre.getStatutOffre());
        assertNotNull(offre.getDatePublication());

        // On ne peut pas publier deux fois.
        assertThrows(RegleMetierException.class, () -> offreService.publier(offre, null, rh()));

        offreService.cloturer(offre, rh());
        assertEquals("CLOTUREE", offre.getStatutOffre());
    }

    // ------------------------------------------------------------------
    // 3. LE PARCOURS DU CANDIDAT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Une candidature ne peut pas sauter les etapes du parcours")
    void uneCandidatureNePeutPasRevenirEnArriere() {

        // La candidature 3 est au statut RECUE : elle ne peut pas etre retenue
        // directement, il faut d'abord passer par l'entretien et l'evaluation.
        Candidature candidature = candidatureRepository.findById(3L).orElseThrow();
        assertEquals("RECUE", candidature.getStatutCandidature());

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> candidatureService.changerStatut(candidature, "RETENUE", rh(), ""));
        assertTrue(e.getMessage().contains("impossible"));
    }

    @Test
    @DisplayName("Une candidature embauchee est definitive")
    void uneCandidatureEmbaucheeEstDefinitive() {

        // La candidature 1 est deja embauchee (voir 02_data.sql).
        Candidature candidature = candidatureRepository.findById(1L).orElseThrow();
        assertEquals("EMBAUCHEE", candidature.getStatutCandidature());

        assertThrows(RegleMetierException.class,
                () -> candidatureService.changerStatut(candidature, "REFUSEE", rh(), ""));
    }

    @Test
    @DisplayName("On ne peut pas postuler deux fois a la meme offre")
    void uneSeuleCandidatureParCandidatEtParOffre() {

        // Le candidat 3 (Marco Rossi) a deja postule a l'offre 2.
        OffreEmploi offre = offreRepository.findById(2L).orElseThrow();

        assertThrows(RegleMetierException.class,
                () -> candidatureService.deposer(offre, "Marco", "Rossi",
                        "marco.rossi@mail.com", "0600000000", "Nouvelle lettre", null));
    }

    @Test
    @DisplayName("On ne peut pas postuler a une offre cloturee")
    void pasDeCandidatureSurUneOffreCloturee() {

        OffreEmploi offre = offreRepository.findById(1L).orElseThrow(); // CLOTUREE
        assertEquals("CLOTUREE", offre.getStatutOffre());

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> candidatureService.deposer(offre, "Paul", "Durand",
                        "paul.durand@mail.com", "0611111111", "", null));
        assertTrue(e.getMessage().contains("ouverte"));
    }

    // ------------------------------------------------------------------
    // 4. LA DECISION D EMBAUCHE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("La decision d'embauche ne porte que sur une candidature retenue")
    void laDecisionExigeUneCandidatureRetenue() {

        // La candidature 3 est au statut RECUE.
        Candidature candidature = candidatureRepository.findById(3L).orElseThrow();

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> decisionService.decider(candidature, true, "", direction()));
        assertTrue(e.getMessage().contains("retenue"));
    }

    @Test
    @DisplayName("Une decision approuvee fait passer la candidature a OFFRE_ENVOYEE")
    void uneDecisionApprouveeFaitAvancer() {

        Candidature candidature = candidatureRepository.findById(3L).orElseThrow();
        candidatureService.changerStatut(candidature, "EN_ENTRETIEN", rh(), "");
        candidatureService.changerStatut(candidature, "EVALUEE", rh(), "");
        candidatureService.changerStatut(candidature, "RETENUE", rh(), "");

        decisionService.decider(candidature, true, "Profil excellent.", direction());

        assertEquals("OFFRE_ENVOYEE", candidature.getStatutCandidature());
    }

    @Test
    @DisplayName("Une decision refusee met la candidature a REFUSEE")
    void uneDecisionRefuseeArrete() {

        Candidature candidature = candidatureRepository.findById(3L).orElseThrow();
        candidatureService.changerStatut(candidature, "EN_ENTRETIEN", rh(), "");
        candidatureService.changerStatut(candidature, "EVALUEE", rh(), "");
        candidatureService.changerStatut(candidature, "RETENUE", rh(), "");

        decisionService.decider(candidature, false, "Non retenu.", direction());

        assertEquals("REFUSEE", candidature.getStatutCandidature());
    }

    // ------------------------------------------------------------------
    // 5. LE DOSSIER EMPLOYE ET LE CONTRAT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Un dossier employe nait seulement d'une offre acceptee")
    void unEmployeNaQuApresAcceptationDeLOffre() {

        Candidature candidature = candidatureRepository.findById(3L).orElseThrow();
        candidatureService.changerStatut(candidature, "EN_ENTRETIEN", rh(), "");

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> employeService.creerDepuisCandidature(candidature, LocalDate.now(), rh()));
        assertTrue(e.getMessage().contains("acceptation"));
    }

    @Test
    @DisplayName("Un contrat avec une date de fin avant la date de debut est refuse")
    void unContratMalDatesEstRefuse() {

        Employe employe = employeRepositoryFind(1L);

        RegleMetierException e = assertThrows(RegleMetierException.class,
                () -> employeService.creerContrat(employe, "CDD",
                        LocalDate.of(2027, 1, 1), LocalDate.of(2026, 1, 1),
                        new BigDecimal("3000"), rh()));
        assertTrue(e.getMessage().contains("fin"));
    }

    @Test
    @DisplayName("Un contrat avec un salaire nul est refuse")
    void unContratSansSalaireEstRefuse() {

        Employe employe = employeRepositoryFind(1L);

        assertThrows(RegleMetierException.class,
                () -> employeService.creerContrat(employe, "CDI",
                        LocalDate.now(), null, BigDecimal.ZERO, rh()));
    }

    // ------------------------------------------------------------------
    // 6. LE GRAND TEST : LE CIRCUIT COMPLET, DE LA DEMANDE A L EMPLOYE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Le circuit complet : demande -> offre -> candidature -> embauche -> contrat")
    void leCircuitComplet() {

        // ---- Etape 1 : le MANAGER cree sa demande ----
        DemandeRecrutement demande = demandeService.creer(poste(1L), departement(3L), manager(),
                "Recrutement d un developpeur pour le portail client.", 1,
                new BigDecimal("45000"), LocalDate.now().plusMonths(3), true);
        assertEquals("EN_ATTENTE_RH", demande.getStatutDemande());

        // ---- Etape 2 : le RH valide ----
        demandeService.valider(demande, "RH", true, "Besoin confirme.", rh());
        assertEquals("EN_ATTENTE_FINANCE", demande.getStatutDemande());

        // ---- Etape 3 : la Finance valide le budget ----
        demandeService.valider(demande, "FINANCE", true, "Budget vote.", finance());
        assertEquals("EN_ATTENTE_DIRECTION", demande.getStatutDemande());

        // ---- Etape 4 : la Direction accepte ----
        demandeService.valider(demande, "DIRECTION", true, "Accord de principe.", direction());
        assertEquals("ACCEPTEE", demande.getStatutDemande());

        // ---- Etape 5 : le RH cree l'offre et la publie ----
        OffreEmploi offre = offreService.creer(demande, "Developpeur Java",
                "Vous developpez les applications internes.", "Casablanca", "CDI",
                LocalDate.now().plusMonths(2), rh());
        offreService.publier(offre, LocalDate.now().plusMonths(2), rh());
        assertEquals("PUBLIEE", offre.getStatutOffre());

        // ---- Etape 6 : un candidat postule ----
        Candidature candidature = candidatureService.deposer(offre, "Lina", "Amrani",
                "lina.amrani@mail.com", "0699999999", "Je souhaite rejoindre l equipe.", null);
        assertEquals("RECUE", candidature.getStatutCandidature());

        // ---- Etape 7 : le RH planifie un entretien ----
        candidatureService.changerStatut(candidature, "EN_ENTRETIEN", rh(), "");
        var entretien = new com.ecole.sirh.model.Entretien();
        entretien.setCandidature(candidature);
        entretien.setDateHeureEntretien(LocalDateTime.now().plusDays(7));
        entretien.setLieuEntretien("Salle RH");
        entretien.setTypeEntretien("PRESENTIEL");
        entretien.setStatutEntretien("PLANIFIE");
        entretienRepository.save(entretien);

        // ---- Etape 8 : le RH saisit son avis ----
        var evaluation = new com.ecole.sirh.model.Evaluation();
        evaluation.setEntretien(entretien);
        evaluation.setEvaluateur(rh());
        evaluation.setNoteEvaluation(4);
        evaluation.setAvisEvaluation("FAVORABLE");
        evaluation.setPointsForts("Bonne base technique.");
        evaluation.setPointsFaibles("Peu d experience.");
        evaluationRepository.save(evaluation);
        candidatureService.changerStatut(candidature, "EVALUEE", rh(), "");
        assertEquals("EVALUEE", candidature.getStatutCandidature());

        // ---- Etape 9 : le RH retient le candidat ----
        candidatureService.changerStatut(candidature, "RETENUE", rh(), "");
        assertEquals("RETENUE", candidature.getStatutCandidature());

        // ---- Etape 10 : la Direction approuve l'embauche ----
        decisionService.decider(candidature, true, "Profil retenu.", direction());
        assertEquals("OFFRE_ENVOYEE", candidature.getStatutCandidature());

        // ---- Etape 11 : le candidat accepte l'offre ----
        candidatureService.changerStatut(candidature, "OFFRE_ACCEPTEE", rh(), "");
        assertEquals("OFFRE_ACCEPTEE", candidature.getStatutCandidature());

        // ---- Etape 12 : le RH cree le dossier employe ----
        Employe employe = employeService.creerDepuisCandidature(candidature,
                LocalDate.now().plusMonths(1), rh());

        assertNotNull(employe.getMatricule());
        assertTrue(employe.getMatricule().startsWith("EMP-"));
        assertEquals("EMBAUCHEE", candidature.getStatutCandidature());
        // Le poste et le departement viennent de la demande d origine.
        assertEquals(demande.getPoste().getIdPoste(), employe.getPoste().getIdPoste());

        // Le plan d integration a ete cree automatiquement (5 taches par defaut).
        assertEquals(5, tacheIntegrationRepository
                .findByEmploye_IdEmployeOrderByDatePrevueAsc(employe.getIdEmploye()).size());

        // ---- Etape 13 : le RH prepare et signe le contrat ----
        Contrat contrat = employeService.creerContrat(employe, "CDI",
                LocalDate.now().plusMonths(1), null, new BigDecimal("44000"), rh());
        assertEquals("BROUILLON", contrat.getStatutContrat());

        employeService.signerContrat(contrat, LocalDate.now(), rh());
        assertEquals("SIGNE", contrat.getStatutContrat());
        assertNotNull(contrat.getDateSignature());

        // Un contrat deja signe ne se signe pas deux fois.
        assertThrows(RegleMetierException.class,
                () -> employeService.signerContrat(contrat, LocalDate.now(), rh()));
    }

    // ------------------------------------------------------------------
    // Les petits utilitaires du test
    // ------------------------------------------------------------------

    private DemandeRecrutement creerDemande(boolean financeRequise) {
        return demandeService.creer(poste(1L), departement(3L), manager(),
                "Demande de test pour le circuit.", 1, new BigDecimal("40000"),
                LocalDate.now().plusMonths(2), financeRequise);
    }

    private Poste poste(Long id) {
        return posteRepository.findById(id).orElseThrow();
    }

    private Departement departement(Long id) {
        return departementRepository.findById(id).orElseThrow();
    }

    private Employe employeRepositoryFind(Long id) {
        return employeService.listerTous().stream()
                .filter(e -> e.getIdEmploye().equals(id))
                .findFirst().orElseThrow();
    }

    private Utilisateur manager()  { return utilisateurRepository.findById(ID_MANAGER).orElseThrow(); }
    private Utilisateur rh()       { return utilisateurRepository.findById(ID_RH).orElseThrow(); }
    private Utilisateur finance()  { return utilisateurRepository.findById(ID_FINANCE).orElseThrow(); }
    private Utilisateur direction(){ return utilisateurRepository.findById(ID_DIRECTION).orElseThrow(); }
}
