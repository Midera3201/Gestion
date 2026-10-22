package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.Contrat;
import com.ecole.sirh.model.Employe;
import com.ecole.sirh.model.TacheIntegration;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.ContratRepository;
import com.ecole.sirh.repository.EmployeRepository;
import com.ecole.sirh.repository.TacheIntegrationRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import com.ecole.sirh.service.EmployeService;
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
 * ETAPE 10 : les dossiers employes.
 *
 *   GET  /employes              -> la liste des employes
 *   GET  /employes/{id}         -> le dossier complet (contrat + integration)
 *   POST /employes/nouveau      -> cree le dossier depuis une candidature acceptee
 *   POST /employes/{id}/statut  -> active / inactive un employe
 *   POST /employes/{id}/contrat -> prepare un contrat
 *   POST /employes/taches/{id}/statut -> fait avancer une tache d integration
 */
@Controller
@RequestMapping("/employes")
public class EmployeController {

    private final EmployeService employeService;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final TacheIntegrationRepository tacheIntegrationRepository;
    private final CandidatureRepository candidatureRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EmployeController(EmployeService employeService,
                             EmployeRepository employeRepository,
                             ContratRepository contratRepository,
                             TacheIntegrationRepository tacheIntegrationRepository,
                             CandidatureRepository candidatureRepository,
                             UtilisateurRepository utilisateurRepository) {
        this.employeService = employeService;
        this.employeRepository = employeRepository;
        this.contratRepository = contratRepository;
        this.tacheIntegrationRepository = tacheIntegrationRepository;
        this.candidatureRepository = candidatureRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("employes", employeService.listerTous());
        model.addAttribute("contrats", employeService.listerContrats());

        // Les candidatures dont l'offre a ete acceptee : candidates a la
        // creation d'un dossier employe.
        model.addAttribute("candidaturesEmbauches",
                candidatureRepository.findAllByStatutCandidatureOrderByDateCandidatureDesc("OFFRE_ACCEPTEE"));

        return "employes";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RegleMetierException("Employe introuvable."));

        List<Contrat> contrats = contratRepository.findByEmploye_IdEmployeOrderByDateDebutDesc(id);
        List<TacheIntegration> taches = tacheIntegrationRepository
                .findByEmploye_IdEmployeOrderByDatePrevueAsc(id);

        model.addAttribute("employe", employe);
        model.addAttribute("contrats", contrats);
        model.addAttribute("taches", taches);

        return "employe-detail";
    }

    @PostMapping("/nouveau")
    public String creer(@RequestParam Long idCandidature,
                        @RequestParam LocalDate dateEmbauche,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        RedirectAttributes redirectAttributes) {

        try {
            Candidature candidature = candidatureRepository.findById(idCandidature)
                    .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            Employe employe = employeService.creerDepuisCandidature(candidature, dateEmbauche, auteur);

            redirectAttributes.addFlashAttribute("message",
                    "Dossier employe " + employe.getMatricule() + " cree. "
                            + "Les taches d'integration ont ete preparees.");
            return "redirect:/employes/" + employe.getIdEmploye();

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employes";
        }
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id,
                                @RequestParam String statut,
                                @AuthenticationPrincipal UtilisateurPrincipal principal,
                                RedirectAttributes redirectAttributes) {

        try {
            if (!"ACTIF".equals(statut) && !"INACTIF".equals(statut)) {
                throw new RegleMetierException("Statut invalide : " + statut);
            }
            Employe employe = employeRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Employe introuvable."));

            employe.setStatutEmploye(statut);
            employeRepository.save(employe);

            redirectAttributes.addFlashAttribute("message",
                    "Statut de " + employe.getMatricule() + " : " + statut + ".");
            return "redirect:/employes/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employes";
        }
    }

    @PostMapping("/{id}/contrat")
    public String preparerContrat(@PathVariable Long id,
                                  @RequestParam(defaultValue = "CDI") String typeContrat,
                                  @RequestParam LocalDate dateDebut,
                                  @RequestParam(required = false) LocalDate dateFin,
                                  @RequestParam BigDecimal salaire,
                                  @AuthenticationPrincipal UtilisateurPrincipal principal,
                                  RedirectAttributes redirectAttributes) {

        try {
            Employe employe = employeRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Employe introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            employeService.creerContrat(employe, typeContrat, dateDebut, dateFin, salaire, auteur);

            redirectAttributes.addFlashAttribute("message", "Contrat prepare pour " + employe.getMatricule() + ".");
            return "redirect:/employes/" + id;

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employes/" + id;
        }
    }

    @PostMapping("/taches/{id}/statut")
    public String changerStatutTache(@PathVariable Long id,
                                     @RequestParam String statut,
                                     @RequestParam(required = false) LocalDate dateRealisation,
                                     @AuthenticationPrincipal UtilisateurPrincipal principal,
                                     RedirectAttributes redirectAttributes) {

        try {
            TacheIntegration tache = tacheIntegrationRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Tache introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            employeService.changerStatutTache(tache, statut, dateRealisation, auteur);

            redirectAttributes.addFlashAttribute("message", "Tache mise a jour : " + statut + ".");
            return "redirect:/employes/" + tache.getEmploye().getIdEmploye();

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employes";
        }
    }
}
