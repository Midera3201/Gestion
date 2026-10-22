package com.ecole.sirh.web;

import com.ecole.sirh.model.Role;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.RoleRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
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

import java.util.List;

/**
 * Espace ADMIN : gestion des comptes utilisateurs et des roles.
 *
 * Rappel du cahier des charges :
 *  - l'ADMIN cree et desactive les comptes
 *  - l'ADMIN attribue les roles
 *  - l'ADMIN NE VALIDE AUCUNE embauche
 *    (c'est regle par le fait que /admin/** est reserve a ADMIN et
 *     que les pages de recrutement exigent RH / FINANCE / DIRECTION)
 */
@Controller
@RequestMapping("/admin/utilisateurs")
public class AdminUtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    public AdminUtilisateurController(UtilisateurRepository utilisateurRepository,
                                      RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        model.addAttribute("roles", roleRepository.findAllByOrderByCodeRoleAsc());
        return "admin/utilisateurs";
    }

    /** Desactiver / reactiver un compte. */
    @PostMapping("/{id}/basculer")
    public String basculer(@PathVariable Long id,
                           @AuthenticationPrincipal UtilisateurPrincipal principal,
                           RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);

        if (utilisateur == null) {
            redirectAttributes.addFlashAttribute("erreur", "Compte introuvable.");
            return "redirect:/admin/utilisateurs";
        }

        // On ne se desactive pas soi-meme : sinon on se ferme la porte.
        if (utilisateur.getIdUtilisateur().equals(principal.getId())) {
            redirectAttributes.addFlashAttribute("erreur",
                    "Vous ne pouvez pas desactiver votre propre compte.");
            return "redirect:/admin/utilisateurs";
        }

        utilisateur.setActif(!utilisateur.isActif());
        utilisateurRepository.save(utilisateur);

        redirectAttributes.addFlashAttribute("message",
                "Le compte de " + utilisateur.getNomComplet() + " est maintenant "
                        + (utilisateur.isActif() ? "actif." : "desactive."));
        return "redirect:/admin/utilisateurs";
    }

    /** Changer le role d'un compte. */
    @PostMapping("/{id}/role")
    public String changerRole(@PathVariable Long id,
                              @RequestParam Long idRole,
                              @AuthenticationPrincipal UtilisateurPrincipal principal,
                              RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        Role role = roleRepository.findById(idRole).orElse(null);

        if (utilisateur == null || role == null) {
            redirectAttributes.addFlashAttribute("erreur", "Compte ou role introuvable.");
            return "redirect:/admin/utilisateurs";
        }

        if (utilisateur.getIdUtilisateur().equals(principal.getId())) {
            redirectAttributes.addFlashAttribute("erreur",
                    "Vous ne pouvez pas changer votre propre role.");
            return "redirect:/admin/utilisateurs";
        }

        utilisateur.setRole(role);
        utilisateurRepository.save(utilisateur);

        redirectAttributes.addFlashAttribute("message",
                "Le role de " + utilisateur.getNomComplet() + " est maintenant " + role.getCodeRole() + ".");
        return "redirect:/admin/utilisateurs";
    }
}
