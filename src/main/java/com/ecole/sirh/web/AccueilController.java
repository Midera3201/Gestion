package com.ecole.sirh.web;

import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Page d'accueil (etapes 3 et 4).
 *
 * On y melange deux facons de lire la base :
 *  - les Repository (JPA) pour les donnees metier : c'est ce qu'on utilise
 *    desormais dans tout le site ;
 *  - le JdbcTemplate pour les tables dont l'entite Java n'existe pas encore
 *    ou pour un simple comptage (candidature, employe).
 */
@Controller
public class AccueilController {

    private final DepartementRepository departementRepository;
    private final PosteRepository posteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeRecrutementRepository demandeRepository;
    private final JdbcTemplate jdbcTemplate;

    public AccueilController(DepartementRepository departementRepository,
                             PosteRepository posteRepository,
                             UtilisateurRepository utilisateurRepository,
                             DemandeRecrutementRepository demandeRepository,
                             JdbcTemplate jdbcTemplate) {
        this.departementRepository = departementRepository;
        this.posteRepository = posteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.demandeRepository = demandeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * URL "/accueil" et non "/" : la racine "/" est desormais le tableau de bord
     * (voir DashboardController). Deux controleurs ne peuvent pas gerer la meme
     * URL, sinon Spring demarre l'application avec l'erreur
     * "Ambiguous mapping".
     */
    @GetMapping("/accueil")
    public String accueil(Model model) {
        model.addAttribute("connecte", true);

        model.addAttribute("nbCandidatures",
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM candidature", Integer.class));
        model.addAttribute("nbEmployes",
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employe", Integer.class));

        // --- Donnees metier (JPA) ---
        model.addAttribute("nbUtilisateurs", utilisateurRepository.count());
        model.addAttribute("nbDepartements", departementRepository.count());
        model.addAttribute("nbPostes", posteRepository.count());
        model.addAttribute("nbDemandes", demandeRepository.count());

        // Les 5 dernieres demandes, avec leur poste et leur demandeur.
        model.addAttribute("demandes", demandeRepository.findAllByOrderByIdDemandeDesc());

        return "accueil";
    }
}
