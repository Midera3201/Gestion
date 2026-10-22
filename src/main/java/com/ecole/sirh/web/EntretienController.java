package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.Entretien;
import com.ecole.sirh.model.Evaluation;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
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

import java.time.LocalDateTime;

/**
 * ETAPE 8 : les entretiens et les evaluations.
 *
 *   GET  /entretiens                 -> le calendrier des entretiens
 *   GET  /entretiens/{id}            -> le detail d'un entretien + les avis
 *   POST /entretiens/nouveau         -> planifier un entretien
 *   POST /entretiens/{id}/statut     -> marquer realise ou annuler
 *   POST /entretiens/{id}/evaluation -> saisir un avis sur le candidat
 *
 * Le MANAGER et le RH peuvent donner un avis. La contrainte UNIQUE
 * (id_entretien, id_evaluateur) dans PostgreSQL interdit a la meme
 * personne de noter deux fois le meme entretien.
 */
@Controller
@RequestMapping("/entretiens")
public class EntretienController {

    private final EntretienRepository entretienRepository;
    private final EvaluationRepository evaluationRepository;
    private final CandidatureRepository candidatureRepository;
    private final CandidatureService candidatureService;
    private final UtilisateurRepository utilisateurRepository;

    public EntretienController(EntretienRepository entretienRepository,
                               EvaluationRepository evaluationRepository,
                               CandidatureRepository candidatureRepository,
                               CandidatureService candidatureService,
                               UtilisateurRepository utilisateurRepository) {
        this.entretienRepository = entretienRepository;
        this.evaluationRepository = evaluationRepository;
        this.candidatureRepository = candidatureRepository;
        this.candidatureService = candidatureService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {

        model.addAttribute("entretiens", entretienRepository.findAllByOrderByDateHeureEntretienDesc());

        // Les candidatures en attente d'un entretien (statut RECUE) : c'est ce que
        // le RH peut encore planifier.
        model.addAttribute("candidaturesPlanifiables",
                candidatureRepository.findAllByStatutCandidatureOrderByDateCandidatureDesc("RECUE"));

        return "entretiens";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Entretien introuvable."));

        model.addAttribute("entretien", entretien);
        model.addAttribute("evaluations",
                evaluationRepository.findByEntretien_IdEntretienOrderByDateEvaluationAsc(id));

        return "entretien-detail";
    }

    @PostMapping("/nouveau")
    public String planifier(@RequestParam Long idCandidature,
                            @RequestParam LocalDateTime dateHeureEntretien,
                            @RequestParam(required = false) String lieuEntretien,
                            @RequestParam(defaultValue = "PRESENTIEL") String typeEntretien,
                            @AuthenticationPrincipal UtilisateurPrincipal principal,
                            RedirectAttributes redirectAttributes) {

        try {
            Candidature candidature = candidatureRepository.findById(idCandidature)
                    .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));

            Entretien entretien = new Entretien();
            entretien.setCandidature(candidature);
            entretien.setDateHeureEntretien(dateHeureEntretien);
            entretien.setLieuEntretien(lieuEntretien);
            entretien.setTypeEntretien(typeEntretien);
            entretien.setStatutEntretien("PLANIFIE");
            entretienRepository.save(entretien);

            // La candidature passe a EN_ENTRETIEN.
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);
            candidatureService.changerStatut(candidature, "EN_ENTRETIEN", auteur,
                    "Entretien planifie le " + dateHeureEntretien + ".");

            redirectAttributes.addFlashAttribute("message",
                    "Entretien planifie le " + dateHeureEntretien + ".");
            return "redirect:/entretiens";

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/entretiens";
        }
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String statut,
                                RedirectAttributes redirectAttributes) {

        try {
            Entretien entretien = entretienRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Entretien introuvable."));

            if (!"PLANIFIE".equals(entretien.getStatutEntretien())) {
                throw new RegleMetierException("Cet entretien n'est plus planifiable.");
            }
            if (!"REALISE".equals(statut) && !"ANNULE".equals(statut)) {
                throw new RegleMetierException("Statut invalide : " + statut);
            }

            entretien.setStatutEntretien(statut);
            entretienRepository.save(entretien);

            redirectAttributes.addFlashAttribute("message",
                    "Entretien " + (("REALISE".equals(statut)) ? "marque comme realise." : "annule."));
            return "redirect:/entretiens";

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/entretiens";
        }
    }

    /**
     * Saisir un avis sur le candidat : note / points forts / points faibles / avis.
     * La candidature passe automatiquement a EVALUEE.
     */
    @PostMapping("/{id}/evaluation")
    public String evaluer(@PathVariable Long id,
                          @RequestParam int noteEvaluation,
                          @RequestParam(required = false) String pointsForts,
                          @RequestParam(required = false) String pointsFaibles,
                          @RequestParam String avisEvaluation,
                          @AuthenticationPrincipal UtilisateurPrincipal principal,
                          RedirectAttributes redirectAttributes) {

        try {
            if (noteEvaluation < 1 || noteEvaluation > 5) {
                throw new RegleMetierException("La note doit etre comprise entre 1 et 5.");
            }
            if (!"FAVORABLE".equals(avisEvaluation)
                    && !"DEFAVORABLE".equals(avisEvaluation)
                    && !"RESERVE".equals(avisEvaluation)) {
                throw new RegleMetierException("Avis invalide : " + avisEvaluation);
            }

            Entretien entretien = entretienRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Entretien introuvable."));

            Utilisateur evaluateur = utilisateurRepository.findById(principal.getId()).orElse(null);

            if (evaluationRepository.existsByEntretien_IdEntretienAndEvaluateur_IdUtilisateur(
                    id, principal.getId())) {
                throw new RegleMetierException("Vous avez deja donne votre avis sur cet entretien.");
            }

            Evaluation evaluation = new Evaluation();
            evaluation.setEntretien(entretien);
            evaluation.setEvaluateur(evaluateur);
            evaluation.setNoteEvaluation(noteEvaluation);
            evaluation.setPointsForts(pointsForts);
            evaluation.setPointsFaibles(pointsFaibles);
            evaluation.setAvisEvaluation(avisEvaluation);
            evaluationRepository.save(evaluation);

            // L'entretien est realise et la candidature est evaluee.
            if ("PLANIFIE".equals(entretien.getStatutEntretien())) {
                entretien.setStatutEntretien("REALISE");
                entretienRepository.save(entretien);
            }
            candidatureService.changerStatut(entretien.getCandidature(), "EVALUEE", evaluateur,
                    "Evaluation de l'entretien du " + entretien.getDateHeureEntretien() + ".");

            redirectAttributes.addFlashAttribute("message", "Votre avis a ete enregistre.");
            return "redirect:/entretiens/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/entretiens";
        }
    }
}
