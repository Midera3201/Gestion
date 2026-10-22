package com.ecole.sirh.web;

import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.Departement;
import com.ecole.sirh.model.Poste;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.repository.ValidationRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import com.ecole.sirh.service.DemandeService;
import com.ecole.sirh.service.RegleMetierException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ETAPE 6 : les demandes de recrutement et leur circuit de validation.
 *
 *   GET  /demandes          -> la liste (different selon le role)
 *   GET  /demandes/{id}     -> le detail + le circuit
 *   GET  /demandes/nouvelle -> le formulaire de creation (MANAGER)
 *   POST /demandes/nouvelle -> la creation (MANAGER)
 *   POST /demandes/{id}/valider -> la validation (RH / FINANCE / DIRECTION)
 *   POST /demandes/{id}/annuler -> l'annulation (le MANAGER demandeur)
 *
 * Le role du connecte est lu avec @AuthenticationPrincipal : on sait
 * toujours qui fait quoi, et on adapte la page en consequence.
 */
@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final DemandeRecrutementRepository demandeRepository;
    private final ValidationRepository validationRepository;
    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DemandeController(DemandeService demandeService,
                             DemandeRecrutementRepository demandeRepository,
                             ValidationRepository validationRepository,
                             PosteRepository posteRepository,
                             DepartementRepository departementRepository,
                             UtilisateurRepository utilisateurRepository) {
        this.demandeService = demandeService;
        this.demandeRepository = demandeRepository;
        this.validationRepository = validationRepository;
        this.posteRepository = posteRepository;
        this.departementRepository = departementRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ------------------------------------------------------------------
    // LA LISTE
    // ------------------------------------------------------------------

    @GetMapping
    public String lister(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {

        String role = principal.getCodeRole();

        // Le MANAGER ne voit que ses propres demandes.
        // Les autres roles (RH, FINANCE, DIRECTION, ADMIN) voient tout le circuit.
        List<DemandeRecrutement> demandes;
        if ("MANAGER".equals(role)) {
            demandes = demandeService.listerParDemandeur(principal.getId());
        } else {
            demandes = demandeService.listerToutes();
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("role", role);
        model.addAttribute("mesDemandes", "MANAGER".equals(role));

        return "demandes";
    }

    // ------------------------------------------------------------------
    // LE DETAIL + LE CIRCUIT
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UtilisateurPrincipal principal,
                         Model model) {

        DemandeRecrutement demande = demandeRepositoryFind(id);

        // Un MANAGER ne peut consulter que ses propres demandes.
        if ("MANAGER".equals(principal.getCodeRole())
                && !demande.getDemandeur().getIdUtilisateur().equals(principal.getId())) {
            model.addAttribute("erreur", "Cette demande ne vous appartient pas.");
            return "redirect:/demandes";
        }

        model.addAttribute("demande", demande);
        model.addAttribute("role", principal.getCodeRole());
        model.addAttribute("etapeAttendue", demandeService.etapeAttendue(demande));

        // Les decisions deja prises sur cette demande (l'historique du circuit).
        model.addAttribute("validations",
                validationRepository.findByDemande_IdDemandeOrderByDateDecisionAsc(demande.getIdDemande()));

        // Le MANAGER ne peut annuler que sa propre demande.
        model.addAttribute("estDemandeur",
                demande.getDemandeur().getIdUtilisateur().equals(principal.getId()));

        return "demande-detail";
    }

    // ------------------------------------------------------------------
    // LA CREATION (MANAGER)
    // ------------------------------------------------------------------

    @GetMapping("/nouvelle")
    public String formulaire(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("postes", posteRepository.findAllByOrderByTitrePosteAsc());
        model.addAttribute("departements", departementRepository.findAllByOrderByNomDepartementAsc());
        model.addAttribute("role", principal.getCodeRole());
        return "demande-nouvelle";
    }

    @PostMapping("/nouvelle")
    public String creer(@RequestParam Long idPoste,
                        @RequestParam Long idDepartement,
                        @RequestParam String motifDemande,
                        @RequestParam(defaultValue = "1") int nombrePostes,
                        @RequestParam(defaultValue = "0") BigDecimal budgetEstime,
                        @RequestParam LocalDate dateBesoin,
                        @RequestParam(defaultValue = "true") boolean financeRequise,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        RedirectAttributes redirectAttributes) {

        try {
            Poste poste = posteRepository.findById(idPoste).orElse(null);
            Departement departement = departementRepository.findById(idDepartement).orElse(null);
            Utilisateur demandeur = utilisateurRepository.findById(principal.getId()).orElse(null);

            DemandeRecrutement demande = demandeService.creer(poste, departement, demandeur,
                    motifDemande, nombrePostes, budgetEstime, dateBesoin, financeRequise);

            redirectAttributes.addFlashAttribute("message",
                    "Demande " + demande.getReferenceDemande() + " creee. Elle est en attente de validation RH.");
            return "redirect:/demandes/" + demande.getIdDemande();

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/demandes/nouvelle";
        }
    }

    // ------------------------------------------------------------------
    // LA VALIDATION (RH / FINANCE / DIRECTION)
    // ------------------------------------------------------------------

    /**
     * Une seule URL pour les 3 etapes : c'est le STATUT de la demande qui
     * dit qui doit signer. Le role de l'utilisateur connecte est compare a
     * l'etape attendue (regle metier dans DemandeService).
     */
    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Long id,
                          @RequestParam(defaultValue = "true") boolean decisionAcceptee,
                          @RequestParam(required = false) String commentaire,
                          @AuthenticationPrincipal UtilisateurPrincipal principal,
                          RedirectAttributes redirectAttributes) {

        try {
            DemandeRecrutement demande = demandeRepositoryFind(id);

            // L'etape a signer est deduite du statut de la demande.
            String etape = demandeService.etapeAttendue(demande);
            if (etape == null) {
                throw new RegleMetierException("Cette demande n'est plus dans le circuit.");
            }

            // Verification : le role connecte doit correspondre a l'etape.
            String role = principal.getCodeRole();
            if (!role.equals(etape)) {
                throw new RegleMetierException("Action refusee : la demande attend l'etape "
                        + etape + " et vous etes " + role + ".");
            }

            Utilisateur valideur = utilisateurRepository.findById(principal.getId()).orElse(null);
            demandeService.valider(demande, etape, decisionAcceptee, commentaire, valideur);

            redirectAttributes.addFlashAttribute("message",
                    decisionAcceptee
                            ? "Demande " + demande.getReferenceDemande() + " validee a l'etape " + etape + "."
                            : "Demande " + demande.getReferenceDemande() + " refusee a l'etape " + etape + ".");
            return "redirect:/demandes/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/demandes";
        }
    }

    // ------------------------------------------------------------------
    // L ANNULATION (le MANAGER demandeur)
    // ------------------------------------------------------------------

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id,
                          @AuthenticationPrincipal UtilisateurPrincipal principal,
                          RedirectAttributes redirectAttributes) {

        try {
            DemandeRecrutement demande = demandeRepositoryFind(id);
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);
            demandeService.annuler(demande, auteur);

            redirectAttributes.addFlashAttribute("message",
                    "Demande " + demande.getReferenceDemande() + " annulee.");
            return "redirect:/demandes";

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/demandes";
        }
    }

    // ------------------------------------------------------------------
    // PETIT UTILITAIRE
    // ------------------------------------------------------------------

    private DemandeRecrutement demandeRepositoryFind(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Demande introuvable."));
    }
}
