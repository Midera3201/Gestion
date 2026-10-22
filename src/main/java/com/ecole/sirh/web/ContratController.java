package com.ecole.sirh.web;

import com.ecole.sirh.model.Contrat;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.ContratRepository;
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

import java.time.LocalDate;

/**
 * ETAPE 10 : la signature des contrats de travail.
 *
 *   GET  /contrats            -> le tableau des contrats
 *   POST /contrats/{id}/signer-> le RH signe le contrat
 */
@Controller
@RequestMapping("/contrats")
public class ContratController {

    private final ContratRepository contratRepository;
    private final EmployeService employeService;
    private final UtilisateurRepository utilisateurRepository;

    public ContratController(ContratRepository contratRepository,
                              EmployeService employeService,
                              UtilisateurRepository utilisateurRepository) {
        this.contratRepository = contratRepository;
        this.employeService = employeService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("contrats", contratRepository.findAllByOrderByDateDebutDesc());
        return "contrats";
    }

    @PostMapping("/{id}/signer")
    public String signer(@PathVariable Long id,
                         @RequestParam(required = false) LocalDate dateSignature,
                         @AuthenticationPrincipal UtilisateurPrincipal principal,
                         RedirectAttributes redirectAttributes) {

        try {
            Contrat contrat = contratRepository.findById(id)
                    .orElseThrow(() -> new RegleMetierException("Contrat introuvable."));
            Utilisateur auteur = utilisateurRepository.findById(principal.getId()).orElse(null);

            employeService.signerContrat(contrat,
                    dateSignature == null ? LocalDate.now() : dateSignature, auteur);

            redirectAttributes.addFlashAttribute("message", "Contrat signe.");
            return "redirect:/employes/" + contrat.getEmploye().getIdEmploye();

        } catch (RegleMetierException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/contrats";
        }
    }
}
