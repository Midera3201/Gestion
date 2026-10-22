package com.ecole.sirh.web;

import com.ecole.sirh.model.Departement;
import com.ecole.sirh.model.Poste;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
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

/**
 * ETAPE 5 (suite) : les postes d'un departement.
 *
 *   GET  /postes           -> la liste des postes
 *   POST /postes/nouveau   -> la creation
 *   POST /postes/{id}/basculer -> activer / desactiver un poste
 *
 * Un poste desactive n'est plus propose dans les demandes de recrutement
 * : on ne supprime jamais une donnee metier, on la desactive.
 */
@Controller
@RequestMapping("/postes")
public class PosteController {

    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;

    public PosteController(PosteRepository posteRepository,
                           DepartementRepository departementRepository) {
        this.posteRepository = posteRepository;
        this.departementRepository = departementRepository;
    }

    @GetMapping
    public String lister(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {
        model.addAttribute("postes", posteRepository.findAllByOrderByTitrePosteAsc());
        model.addAttribute("departements", departementRepository.findAllByOrderByNomDepartementAsc());
        model.addAttribute("role", principal.getCodeRole());
        return "postes";
    }

    @PostMapping("/nouveau")
    public String creer(@RequestParam String titrePoste,
                        @RequestParam(required = false) String descriptionPoste,
                        @RequestParam(required = false) BigDecimal salaireMin,
                        @RequestParam(required = false) BigDecimal salaireMax,
                        @RequestParam Long idDepartement,
                        RedirectAttributes redirectAttributes) {

        if (titrePoste == null || titrePoste.isBlank()) {
            redirectAttributes.addFlashAttribute("erreur", "Le titre du poste est obligatoire.");
            return "redirect:/postes";
        }
        if (salaireMin != null && salaireMax != null && salaireMin.compareTo(salaireMax) > 0) {
            redirectAttributes.addFlashAttribute("erreur",
                    "Le salaire minimum ne peut pas etre superieur au salaire maximum.");
            return "redirect:/postes";
        }

        Departement departement = departementRepository.findById(idDepartement).orElse(null);
        if (departement == null) {
            redirectAttributes.addFlashAttribute("erreur", "Le departement est introuvable.");
            return "redirect:/postes";
        }

        Poste poste = new Poste();
        poste.setTitrePoste(titrePoste.trim());
        poste.setDescriptionPoste(descriptionPoste);
        poste.setSalaireMin(salaireMin);
        poste.setSalaireMax(salaireMax);
        poste.setActif(true);
        poste.setDepartement(departement);
        posteRepository.save(poste);

        redirectAttributes.addFlashAttribute("message",
                "Poste '" + poste.getTitrePoste() + "' cree dans le departement "
                        + departement.getNomDepartement() + ".");
        return "redirect:/postes";
    }

    @PostMapping("/{id}/basculer")
    public String basculer(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Poste poste = posteRepository.findById(id).orElse(null);
        if (poste == null) {
            redirectAttributes.addFlashAttribute("erreur", "Poste introuvable.");
            return "redirect:/postes";
        }

        poste.setActif(!poste.isActif());
        posteRepository.save(poste);

        redirectAttributes.addFlashAttribute("message",
                "Le poste '" + poste.getTitrePoste() + "' est maintenant "
                        + (poste.isActif() ? "actif." : "desactive."));
        return "redirect:/postes";
    }
}
