package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.DecisionEmbauche;
import com.ecole.sirh.model.Entretien;
import com.ecole.sirh.model.Evaluation;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.DecisionEmbaucheRepository;
import com.ecole.sirh.repository.EntretienRepository;
import com.ecole.sirh.repository.EvaluationRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import com.ecole.sirh.service.CandidatureService;
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

import java.util.List;

/**
 * ETAPE 7 et 8 (cote RH) : le suivi des candidatures.
 *
 *   GET  /candidatures          -> toutes les candidatures
 *   GET  /candidatures/{id}     -> le dossier du candidat (entretiens, avis)
 *   POST /candidatures/{id}/statut -> changement de statut du circuit
 *
 * Les changements de statut passent tous par CandidatureService, qui
 * verifie que le passage demande est autorise.
 */
@Controller
@RequestMapping("/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final CandidatureRepository candidatureRepository;
    private final EntretienRepository entretienRepository;
    private final EvaluationRepository evaluationRepository;
    private final DecisionEmbaucheRepository decisionEmbaucheRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CandidatureController(CandidatureService candidatureService,
                                 CandidatureRepository candidatureRepository,
                                 EntretienRepository entretienRepository,
                                 EvaluationRepository evaluationRepository,
                                 DecisionEmbaucheRepository decisionEmbaucheRepository,
                                 UtilisateurRepository utilisateurRepository) {
        this.candidatureService = candidatureService;
        this.candidatureRepository = candidatureRepository;
        this.entretienRepository = entretienRepository;
        this.evaluationRepository = evaluationRepository;
        this.decisionEmbaucheRepository = decisionEmbaucheRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("candidatures", candidatureService.listerToutes());
        return "candidatures";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));

        List<Entretien> entretiens = entretienRepository
                .findByCandidature_IdCandidatureOrderByDateHeureEntretienDesc(id);

        model.addAttribute("candidature", candidature);
        model.addAttribute("entretiens", entretiens);
        model.addAttribute("evaluations", evaluationRepository.findAllByOrderByDateEvaluationDesc());
        model.addAttribute("decision", decisionEmbaucheRepository
                .findByCandidature_IdCandidature(id).orElse(null));

        return "candidature-detail";
    }

    /**
     * Changer le statut d'une candidature.
     * Les valeurs possibles sont : REFUSEE, RETENUE, OFFRE_ENVOYEE, OFFRE_ACCEPTEE.
     */
    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String statut,
                                @RequestParam(required = false) String commentaire,
                                @AuthenticationPrincipal UtilisateurPrincipal principal,
                                RedirectAttributes redirectAttributes) {

        try {
            Candidature candidature = candidatureRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
            var auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            candidatureService.changerStatut(candidature, statut, auteur, commentaire);

            redirectAttributes.addFlashAttribute("message",
                    "Statut de la candidature mis a jour : " + statut + ".");
            return "redirect:/candidatures/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/candidatures";
        }
    }
}
