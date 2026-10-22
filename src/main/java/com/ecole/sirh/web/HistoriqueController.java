package com.ecole.sirh.web;

import com.ecole.sirh.repository.HistoriqueActionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ETAPE 11 : le journal des actions (tracabilite).
 *
 *   GET /historique -> les 50 dernieres actions du site
 *
 * Cette page est reservee a l'ADMIN (voir SecurityConfig) : c'est lui qui
 * controle le site, mais il ne participe pas aux decisions metier.
 */
@Controller
@RequestMapping("/historique")
public class HistoriqueController {

    private final HistoriqueActionRepository historiqueActionRepository;

    public HistoriqueController(HistoriqueActionRepository historiqueActionRepository) {
        this.historiqueActionRepository = historiqueActionRepository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("actions", historiqueActionRepository.findTop50ByOrderByDateActionDesc());
        return "historique";
    }
}
