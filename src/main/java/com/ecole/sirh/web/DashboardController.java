package com.ecole.sirh.web;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.OffreEmploiRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import com.ecole.sirh.security.UtilisateurPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Le tableau de bord : la premiere page que voit un utilisateur connecte.
 *
 * Le contenu depend du role : c'est le principe "role -> droits".
 *  MANAGER   : ses propres demandes
 *  RH        : les demandes en attente de verification RH
 *  FINANCE   : les demandes en attente de verification du budget
 *  DIRECTION : les demandes en attente de decision finale
 *  ADMIN     : pas de travail de recrutement (c'est volontaire)
 */
@Controller
public class DashboardController {

    private final DemandeRecrutementRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PosteRepository posteRepository;
    private final OffreEmploiRepository offreRepository;
    private final CandidatureRepository candidatureRepository;

    public DashboardController(DemandeRecrutementRepository demandeRepository,
                               UtilisateurRepository utilisateurRepository,
                               DepartementRepository departementRepository,
                               PosteRepository posteRepository,
                               OffreEmploiRepository offreRepository,
                               CandidatureRepository candidatureRepository) {
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.departementRepository = departementRepository;
        this.posteRepository = posteRepository;
        this.offreRepository = offreRepository;
        this.candidatureRepository = candidatureRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UtilisateurPrincipal principal, Model model) {

        String role = principal.getCodeRole();

        model.addAttribute("role", role);
        model.addAttribute("nomUtilisateur", principal.getNomComplet());

        // Compteurs generals
        model.addAttribute("nbDemandes", demandeRepository.count());
        model.addAttribute("nbUtilisateurs", utilisateurRepository.count());
        model.addAttribute("nbDepartements", departementRepository.count());
        model.addAttribute("nbPostes", posteRepository.count());
        model.addAttribute("nbOffres", offreRepository.count());
        model.addAttribute("nbCandidatures", candidatureRepository.count());

        // La liste "a traiter" depend du role de l'utilisateur connecte.
        List<DemandeRecrutement> aTraiter = List.of();

        if ("MANAGER".equals(role)) {
            aTraiter = demandeRepository.findByDemandeur_IdUtilisateurOrderByIdDemandeDesc(principal.getId());
        } else if ("RH".equals(role)) {
            aTraiter = demandeRepository.findByStatutDemandeOrderByIdDemandeDesc("EN_ATTENTE_RH");
        } else if ("FINANCE".equals(role)) {
            aTraiter = demandeRepository.findByStatutDemandeOrderByIdDemandeDesc("EN_ATTENTE_FINANCE");
        } else if ("DIRECTION".equals(role)) {
            aTraiter = demandeRepository.findByStatutDemandeOrderByIdDemandeDesc("EN_ATTENTE_DIRECTION");
        }

        model.addAttribute("demandes", aTraiter);
        model.addAttribute("titreListe", titreListe(role));

        // La DIRECTION doit decider des embauches : c'est son file de travail.
        List<Candidature> aDecider = List.of();
        if ("DIRECTION".equals(role)) {
            aDecider = candidatureRepository
                    .findAllByStatutCandidatureOrderByDateCandidatureDesc("RETENUE");
        } else if ("RH".equals(role)) {
            aDecider = candidatureRepository
                    .findAllByStatutCandidatureOrderByDateCandidatureDesc("RECUE");
        }
        model.addAttribute("candidatures", aDecider);

        return "dashboard";
    }

    private String titreListe(String role) {
        return switch (role) {
            case "MANAGER" -> "Mes demandes de recrutement";
            case "RH" -> "Demandes a verifier (etape RH)";
            case "FINANCE" -> "Demandes a verifier (budget)";
            case "DIRECTION" -> "Demandes a decider";
            default -> "Aucune demande a traiter pour ce role";
        };
    }
}
