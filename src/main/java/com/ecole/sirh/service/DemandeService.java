package com.ecole.sirh.service;

import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.Departement;
import com.ecole.sirh.model.Poste;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.model.Validation;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.ValidationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * LE CIRCUIT DE VALIDATION D'UNE DEMANDE DE RECRUTEMENT.
 *
 * Une demande passe par 3 etapes maximum :
 *
 *    EN_ATTENTE_RH  --(le RH valide)-->  EN_ATTENTE_FINANCE  --(la Finance valide)-->  EN_ATTENTE_DIRECTION
 *            |                                    |                                            |
 *            | si finance_requise = false         |                                            | (la Direction valide)
 *            +------------------------------------+------------------------------>  ACCEPTEE
 *                                                                                     ou REFUSEE
 *
 * Un refus arrete immediatement le circuit : la demande passe a REFUSEE.
 *
 * Regle de separation des pouvoirs : chaque etape est signee par un role
 * different (RH, FINANCE, DIRECTION), et l'ADMIN ne valide jamais rien.
 */
@Service
public class DemandeService {

    // Les statuts de la table demande_recrutement (contrainte CHECK du SQL).
    public static final String EN_ATTENTE_RH = "EN_ATTENTE_RH";
    public static final String EN_ATTENTE_FINANCE = "EN_ATTENTE_FINANCE";
    public static final String EN_ATTENTE_DIRECTION = "EN_ATTENTE_DIRECTION";
    public static final String ACCEPTEE = "ACCEPTEE";
    public static final String REFUSEE = "REFUSEE";
    public static final String ANNULEE = "ANNULEE";

    private final DemandeRecrutementRepository demandeRepository;
    private final ValidationRepository validationRepository;
    private final HistoriqueService historiqueService;

    public DemandeService(DemandeRecrutementRepository demandeRepository,
                          ValidationRepository validationRepository,
                          HistoriqueService historiqueService) {
        this.demandeRepository = demandeRepository;
        this.validationRepository = validationRepository;
        this.historiqueService = historiqueService;
    }

    // ------------------------------------------------------------------
    // 1. CREATION D'UNE DEMANDE (par un MANAGER)
    // ------------------------------------------------------------------

    @Transactional
    public DemandeRecrutement creer(Poste poste, Departement departement, Utilisateur demandeur,
                                    String motif, int nombrePostes, BigDecimal budget,
                                    LocalDate dateBesoin, boolean financeRequise) {

        if (poste == null) {
            throw new RegleMetierException("Le poste est obligatoire.");
        }
        if (departement == null) {
            throw new RegleMetierException("Le departement est obligatoire.");
        }
        if (motif == null || motif.isBlank()) {
            throw new RegleMetierException("Le motif de la demande est obligatoire.");
        }
        if (nombrePostes < 1) {
            throw new RegleMetierException("Le nombre de postes doit etre superieur a 0.");
        }
        if (budget == null || budget.signum() < 0) {
            throw new RegleMetierException("Le budget estime ne peut pas etre negatif.");
        }
        if (dateBesoin == null) {
            throw new RegleMetierException("La date de besoin est obligatoire.");
        }

        DemandeRecrutement demande = new DemandeRecrutement();
        demande.setReferenceDemande(prochaineReference());
        demande.setMotifDemande(motif.trim());
        demande.setPoste(poste);
        demande.setDepartement(departement);
        demande.setDemandeur(demandeur);
        demande.setNombrePostes(nombrePostes);
        demande.setBudgetEstime(budget);
        demande.setDateBesoin(dateBesoin);
        demande.setFinanceRequise(financeRequise);
        demande.setStatutDemande(EN_ATTENTE_RH);

        demandeRepository.save(demande);

        historiqueService.enregistrer(demandeur, "CREATION_DEMANDE", "demande_recrutement",
                demande.getIdDemande().intValue(),
                "Demande " + demande.getReferenceDemande() + " creee pour le poste "
                        + poste.getTitrePoste() + ".");

        return demande;
    }

    /**
     * Genere la reference de la demande : DEM-2026-001, DEM-2026-002...
     * On boucle tant que la reference existe deja (securite).
     */
    private String prochaineReference() {
        String annee = String.valueOf(LocalDate.now().getYear());
        int numero = (int) demandeRepository.count() + 1;
        String reference;
        do {
            reference = "DEM-" + annee + "-" + String.format("%03d", numero);
            numero++;
        } while (demandeRepository.findByReferenceDemande(reference).isPresent());
        return reference;
    }

    // ------------------------------------------------------------------
    // 2. L ETAPE ATTENDUE : a qui appartient la demande ?
    // ------------------------------------------------------------------

    /**
     * Retourne le role qui doit traiter la demande, ou null si la demande
     * n'est plus dans le circuit (acceptee, refusee ou annulee).
     */
    public String etapeAttendue(DemandeRecrutement demande) {
        if (demande == null) {
            return null;
        }
        switch (demande.getStatutDemande()) {
            case EN_ATTENTE_RH:
                return "RH";
            case EN_ATTENTE_FINANCE:
                return "FINANCE";
            case EN_ATTENTE_DIRECTION:
                return "DIRECTION";
            default:
                return null;
        }
    }

    /** La demande est-elle encore dans le circuit de validation ? */
    public boolean dansLeCircuit(DemandeRecrutement demande) {
        return etapeAttendue(demande) != null;
    }

    // ------------------------------------------------------------------
    // 3. VALIDATION D'UNE ETAPE
    // ------------------------------------------------------------------

    /**
     * Fait signer une etape du circuit.
     *
     * @param etape          "RH", "FINANCE" ou "DIRECTION"
     * @param decisionAcceptee true = validation, false = refus
     */
    @Transactional
    public DemandeRecrutement valider(DemandeRecrutement demande, String etape,
                                      boolean decisionAcceptee, String commentaire,
                                      Utilisateur valideur) {

        // 1. La demande doit encore etre dans le circuit
        String attendue = etapeAttendue(demande);
        if (attendue == null) {
            throw new RegleMetierException("La demande " + demande.getReferenceDemande()
                    + " n'est plus dans le circuit de validation.");
        }

        // 2. On ne peut valider QUE son etape (separation des pouvoirs)
        if (!attendue.equals(etape)) {
            throw new RegleMetierException("Ce n'est pas votre tour : cette demande attend "
                    + "l'etape " + attendue + ".");
        }

        // 3. Une etape ne se signe qu'une fois (contrainte UNIQUE en base)
        if (validationRepository.findByDemande_IdDemandeAndEtapeValidation(
                demande.getIdDemande(), etape).isPresent()) {
            throw new RegleMetierException("L'etape " + etape + " a deja ete traitee.");
        }

        // 4. On enregistre la decision
        Validation validation = new Validation();
        validation.setEtapeValidation(etape);
        validation.setDecisionValidation(decisionAcceptee ? "VALIDEE" : "REFUSEE");
        validation.setCommentaireValidation(commentaire);
        validation.setDemande(demande);
        validation.setValideur(valideur);
        validationRepository.save(validation);

        // 5. On calcule le nouveau statut de la demande
        String nouveauStatut;
        if (!decisionAcceptee) {
            nouveauStatut = REFUSEE;
        } else if ("RH".equals(etape)) {
            nouveauStatut = demande.isFinanceRequise() ? EN_ATTENTE_FINANCE : EN_ATTENTE_DIRECTION;
        } else if ("FINANCE".equals(etape)) {
            nouveauStatut = EN_ATTENTE_DIRECTION;
        } else {
            nouveauStatut = ACCEPTEE;
        }

        demande.setStatutDemande(nouveauStatut);
        demandeRepository.save(demande);

        historiqueService.enregistrer(valideur, "VALIDATION_DEMANDE", "demande_recrutement",
                demande.getIdDemande().intValue(),
                "Etape " + etape + " : " + (decisionAcceptee ? "validee" : "refusee")
                        + ". Nouveau statut : " + nouveauStatut + ".");

        return demande;
    }

    // ------------------------------------------------------------------
    // 4. ANNULATION (par le demandeur MANAGER)
    // ------------------------------------------------------------------

    @Transactional
    public DemandeRecrutement annuler(DemandeRecrutement demande, Utilisateur auteur) {

        if (!dansLeCircuit(demande)) {
            throw new RegleMetierException("Cette demande n'est plus annulable.");
        }

        // Seul le demandeur (ou un RH) peut annuler la demande.
        boolean estDemandeur = demande.getDemandeur().getIdUtilisateur().equals(auteur.getIdUtilisateur());
        boolean estRh = "RH".equals(auteur.getRole().getCodeRole());
        if (!estDemandeur && !estRh) {
            throw new RegleMetierException("Seul le demandeur ou le RH peut annuler une demande.");
        }

        demande.setStatutDemande(ANNULEE);
        demandeRepository.save(demande);

        historiqueService.enregistrer(auteur, "ANNULATION_DEMANDE", "demande_recrutement",
                demande.getIdDemande().intValue(),
                "Demande " + demande.getReferenceDemande() + " annulee.");

        return demande;
    }

    // ------------------------------------------------------------------
    // 5. LECTURE
    // ------------------------------------------------------------------

    /** Toutes les demandes, de la plus recente a la plus ancienne. */
    public List<DemandeRecrutement> listerToutes() {
        return demandeRepository.findAllByOrderByIdDemandeDesc();
    }

    /** Les demandes d'un demandeur precis. */
    public List<DemandeRecrutement> listerParDemandeur(Long idUtilisateur) {
        return demandeRepository.findByDemandeur_IdUtilisateurOrderByIdDemandeDesc(idUtilisateur);
    }

    /** Les demandes en attente a l'etape donnee (la file de travail du role). */
    public List<DemandeRecrutement> listerParStatut(String statut) {
        return demandeRepository.findByStatutDemandeOrderByIdDemandeDesc(statut);
    }
}
