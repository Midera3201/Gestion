package com.ecole.sirh.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * La page de connexion.
 * Spring Security gere le traitement du formulaire : il verifie
 * l'email et le mot de passe dans la base, puis ouvre une session.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
