package com.ecole.sirh.web;

import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.OffreEmploiRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import com.ecole.sirh.service.OffreService;
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

import java.time.LocalDate;
import java.util.List;

/**
 * ETAPE 7 (cote RH) : la gestion des offres d'emploi.
 *
 *   GET  /offres            -> toutes les offres
 *   GET  /offres/{id}       -> le detail d'une offre et ses candidatures
 *   POST /offres/nouvelle   -> creer l'offre a partir d'une demande acceptee
 *   POST /offres/{id}/publier
 *   POST /offres/{id}/cloturer
 *
 * L'offre est toujours creee a partir d'une demande ACCEPTEE : on ne peut
 * pas recruter sans demande validee.
 */
@Controller
@RequestMapping("/offres")
public class OffreController {

    private final OffreService offreService;
    private final OffreEmploiRepository offreEmploiRepository;
    private final DemandeRecrutementRepository demandeRecrutementRepository;
    private final CandidatureRepository candidatureRepository;
    private final UtilisateurRepository utilisateurRepository;

    public OffreController(OffreService offreService,
                           OffreEmploiRepository offreEmploiRepository,
                           DemandeRecrutementRepository demandeRecrutementRepository,
                           CandidatureRepository candidatureRepository,
                           UtilisateurRepository utilisateurRepository) {
        this.offreService = offreService;
        this.offreEmploiRepository = offreEmploiRepository;
        this.demandeRecrutementRepository = demandeRecrutementRepository;
        this.candidatureRepository = candidatureRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {

        model.addAttribute("offres", offreService.listerToutes());

        // Les demandes acceptees qui n'ont pas encore d'offre : c'est la
        // liste des offres a creer.
        List<DemandeRecrutement> demandesAcceptees = demandeRecrutementRepository
                .findByStatutDemandeOrderByIdDemandeDesc("ACCEPTEE");
        model.addAttribute("demandesAcceptees", demandesAcceptees);

        return "offres";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        OffreEmploi offre = offreEmploiRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Offre introuvable."));

        model.addAttribute("offre", offre);
        model.addAttribute("candidatures", candidatureRepository
                .findByOffre_IdOffreOrderByDateCandidatureDesc(id));
        model.addAttribute("demande", offre.getDemande());

        return "offre-detail";
    }

    @PostMapping("/nouvelle")
    public String creer(@RequestParam Long idDemande,
                        @RequestParam String titreOffre,
                        @RequestParam String descriptionOffre,
                        @RequestParam(required = false) String lieuOffre,
                        @RequestParam(defaultValue = "CDI") String typeContrat,
                        @RequestParam(required = false) LocalDate dateCloture,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        RedirectAttributes redirectAttributes) {

        try {
            DemandeRecrutement demande = demandeRecrutementRepository.findById(idDemande)
                    .orElseThrow(() -> new RegleMetierException("Demande introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            OffreEmploi offre = offreService.creer(demande, titreOffre, descriptionOffre,
                    lieuOffre, typeContrat, dateCloture, auteur);

            redirectAttributes.addFlashAttribute("message",
                    "Offre '" + offre.getTitreOffre() + "' creee en brouillon. Publiez-la pour la rendre visible.");
            return "redirect:/offres/" + offre.getIdOffre();

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/offres";
        }
    }

    @PostMapping("/{id}/publier")
    public String publier(@PathVariable Long id,
                          @RequestParam(required = false) LocalDate dateCloture,
                          @AuthenticationPrincipal UtilisateurPrincipal principal,
                          RedirectAttributes redirectAttributes) {

        try {
            OffreEmploi offre = offreEmploiRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Offre introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            offreService.publier(offre, dateCloture, auteur);

            redirectAttributes.addFlashAttribute("message",
                    "Offre '" + offre.getTitreOffre() + "' publiee. Elle apparait maintenant sur le site public.");
            return "redirect:/offres/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/offres";
        }
    }

    @PostMapping("/{id}/cloturer")
    public String cloturer(@PathVariable Long id,
                           @AuthenticationPrincipal UtilisateurPrincipal principal,
                           RedirectAttributes redirectAttributes) {

        try {
            OffreEmploi offre = offreEmploiRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Offre introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            offreService.cloturer(offre, auteur);

            redirectAttributes.addFlashAttribute("message",
                    "Offre '" + offre.getTitreOffre() + "' cloturee.");
            return "redirect:/offres/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/offres";
        }
    }
}
