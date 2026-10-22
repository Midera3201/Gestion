package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.repository.OffreEmploiRepository;
import com.ecole.sirh.service.CandidatureService;
import com.ecole.sirh.service.OffreService;
import com.ecole.sirh.service.RegleMetierException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ETAPE 7 (cote public) : le site "carrieres" de l'ecole.
 *
 * Ces pages sont accessibles a tout le monde, meme sans etre connecte
 * (voir SecurityConfig : requestMatchers("/carrieres/**").permitAll()).
 *
 *   GET  /carrieres               -> les offres publiees
 *   GET  /carrieres/{id}          -> le detail d'une offre + le formulaire
 *   POST /carrieres/{id}/postuler -> la candidature
 *
 * C'est le seul endroit du site ou l'on cree une candidature sans etre
 * connecte : c'est le candidat lui-meme qui postule.
 */
@Controller
@RequestMapping("/carrieres")
public class CarriereController {

    private final OffreService offreService;
    private final OffreEmploiRepository offreEmploiRepository;
    private final CandidatureService candidatureService;

    public CarriereController(OffreService offreService,
                              OffreEmploiRepository offreEmploiRepository,
                              CandidatureService candidatureService) {
        this.offreService = offreService;
        this.offreEmploiRepository = offreEmploiRepository;
        this.candidatureService = candidatureService;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("offres", offreService.listerPubliees());
        return "carrieres";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        OffreEmploi offre = offreEmploiRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Offre introuvable."));

        // Seules les offres publiees sont visibles du public.
        if (!"PUBLIEE".equals(offre.getStatutOffre())) {
            model.addAttribute("erreur", "Cette offre n'est plus ouverte.");
            return "redirect:/carrieres";
        }

        model.addAttribute("offre", offre);
        model.addAttribute("demande", offre.getDemande());
        return "carriere-offre";
    }

    @PostMapping("/{id}/postuler")
    public String postuler(@PathVariable Long id,
                           @RequestParam String prenom,
                           @RequestParam String nom,
                           @RequestParam String email,
                           @RequestParam(required = false) String telephone,
                           @RequestParam(required = false) String lettreMotivation,
                           RedirectAttributes redirectAttributes) {

        try {
            OffreEmploi offre = offreEmploiRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Offre introuvable."));

            // auteur = null : le candidat n'est pas un utilisateur du site.
            Candidature candidature = candidatureService.deposer(offre, prenom, nom, email,
                    telephone, lettreMotivation, null);

            redirectAttributes.addFlashAttribute("message",
                    "Votre candidature a bien ete enregistree (reference "
                            + candidature.getIdCandidature() + ").");
            return "redirect:/carrieres/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/carrieres/" + id;
        }
    }
}
