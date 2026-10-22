package com.ecole.sirh.web;

import com.ecole.sirh.model.Departement;
import com.ecole.sirh.repository.DepartementRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Premiere vraie page metier du site : les departements.
 *
 * GET  /departements        -> la liste
 * POST /departements/nouveau-> la creation (le bouton du formulaire)
 *
 * Le POST renvoie une redirection ("redirect:/departements") : c'est le
 * pattern "Post/Redirect/Get". Le navigateur demande alors la page
 * comme si rien ne s'etait passe, donc pas de probleme de
 * "formulaire renvoye deux fois" quand on recharge la page.
 */
@Controller
@RequestMapping("/departements")
public class DepartementController {

    private final DepartementRepository departementRepository;

    public DepartementController(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("departements", departementRepository.findAllByOrderByNomDepartementAsc());
        return "departements";
    }

    @PostMapping("/nouveau")
    public String creer(@RequestParam String nomDepartement,
                        @RequestParam(required = false) String descriptionDepartement,
                        @RequestParam(defaultValue = "0") BigDecimal budgetAnnuel,
                        RedirectAttributes redirectAttributes) {

        // 1. Verification du nom
        if (nomDepartement == null || nomDepartement.isBlank()) {
            redirectAttributes.addFlashAttribute("erreur", "Le nom du departement est obligatoire.");
            return "redirect:/departements";
        }

        // 2. Verification de l'unicite (la colonne est UNIQUE en base aussi,
        //    mais on evite une erreur SQL brute pour l'utilisateur)
        if (departementRepository.existsByNomDepartement(nomDepartement.trim())) {
            redirectAttributes.addFlashAttribute("erreur",
                    "Le departement '" + nomDepartement.trim() + "' existe deja.");
            return "redirect:/departements";
        }

        // 3. Enregistrement reel dans PostgreSQL
        Departement departement = new Departement(nomDepartement.trim(),
                descriptionDepartement, budgetAnnuel);
        departementRepository.save(departement);

        // 4. Message de succes affiche apres la redirection
        redirectAttributes.addFlashAttribute("message",
                "Departement '" + departement.getNomDepartement() + "' cree avec succes.");
        return "redirect:/departements";
    }
}
