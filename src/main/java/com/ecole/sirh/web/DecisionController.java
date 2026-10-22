package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.DecisionEmbaucheRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import com.ecole.sirh.service.DecisionService;
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

/**
 * ETAPE 9 : la decision finale d'embauche.
 *
 *   GET  /decisions                   -> les candidatures a decider + l'historique
 *   POST /candidatures/{id}/decision  -> la decision de la DIRECTION
 *
 * Cette page est reservee a la DIRECTION : c'est la separation des pouvoirs
 * demandee par le cahier des charges. Ni le RH, ni le MANAGER, ni l'ADMIN
 * ne peuvent decider une embauche.
 */
@Controller
@RequestMapping("/decisions")
public class DecisionController {

    private final DecisionService decisionService;
    private final CandidatureRepository candidatureRepository;
    private final DecisionEmbaucheRepository decisionEmbaucheRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DecisionController(DecisionService decisionService,
                              CandidatureRepository candidatureRepository,
                              DecisionEmbaucheRepository decisionEmbaucheRepository,
                              UtilisateurRepository utilisateurRepository) {
        this.decisionService = decisionService;
        this.candidatureRepository = candidatureRepository;
        this.decisionEmbaucheRepository = decisionEmbaucheRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {

        // Les candidatures retenues qui attendent une decision.
        model.addAttribute("aDecider",
                candidatureRepository.findAllByStatutCandidatureOrderByDateCandidatureDesc("RETENUE"));

        // Les decisions deja prises.
        model.addAttribute("decisions", decisionEmbaucheRepository.findAllByOrderByDateDecisionDesc());

        return "decisions";
    }

    @PostMapping("/candidatures/{id}/decision")
    public String decider(@PathVariable Long id,
                          @RequestParam(defaultValue = "true") boolean approuvee,
                          @RequestParam(required = false) String commentaire,
                          @AuthenticationPrincipal UtilisateurPrincipal principal,
                          RedirectAttributes redirectAttributes) {

        try {
            Candidature candidature = candidatureRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
            Utilisateur decideur = utilisateurRepository.findById(principal.getId()).orElse(null);

            decisionService.decider(candidature, approuvee, commentaire, decideur);

            redirectAttributes.addFlashAttribute("message",
                    approuvee
                            ? "Embauche approuvee pour " + candidature.getCandidat().getPrenomCandidat()
                              + " " + candidature.getCandidat().getNomCandidat() + "."
                            : "Embauche refusee pour " + candidature.getCandidat().getPrenomCandidat()
                              + " " + candidature.getCandidat().getNomCandidat() + ".");
            return "redirect:/decisions";

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/decisions";
        }
    }
}
