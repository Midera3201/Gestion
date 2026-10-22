package com.ecole.sirh.config;

import com.ecole.sirh.security.UtilisateurDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * TOUTE la configuration de securite du site est dans cette classe.
 *
 * Deux choses a comprendre :
 *
 * 1. Les URL : on dit qui a le droit d accessing quoi.
 *      .permitAll()        -> accessible par tout le monde (page de connexion)
 *      .hasRole("ADMIN")   -> reserve au role ADMIN
 *      .hasAnyRole("RH","ADMIN")
 *      .authenticated()    -> il faut etre connecte (peut-importe le role)
 *
 * 2. hasRole("RH") teste en realite l'autorite "ROLE_RH".
 *    C'est pour cela qu'on a ajoute le prefixe "ROLE_" dans
 *    UtilisateurPrincipal.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UtilisateurDetailsServiceImpl utilisateurDetailsService;

    public SecurityConfig(UtilisateurDetailsServiceImpl utilisateurDetailsService) {
        this.utilisateurDetailsService = utilisateurDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(requests -> requests
                // ressources statiques et page de connexion : ouvertes a tous
                .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**").permitAll()

                // le site public "carrieres" : accessible sans etre connecte
                .requestMatchers("/carrieres", "/carrieres/**").permitAll()

                // espace administration : ADMIN uniquement
                .requestMatchers("/admin/**", "/historique").hasRole("ADMIN")

                // creation d'un departement : RH ou ADMIN uniquement
                .requestMatchers("/departements/nouveau").hasAnyRole("RH", "ADMIN")

                // les postes : RH ou ADMIN
                .requestMatchers("/postes/**").hasAnyRole("RH", "ADMIN")

                // une demande se cree par un MANAGER, elle se signe par RH / FINANCE / DIRECTION
                .requestMatchers("/demandes/nouvelle").hasRole("MANAGER")
                .requestMatchers("/demandes/*/valider").hasAnyRole("RH", "FINANCE", "DIRECTION")
                .requestMatchers("/demandes/*/annuler").hasAnyRole("MANAGER", "RH")

                // la gestion des offres, candidatures et dossiers employes : RH
                .requestMatchers("/offres/**", "/candidatures/**", "/employes/**", "/contrats/**")
                    .hasAnyRole("RH", "ADMIN")

                // les entretiens et les avis : RH et MANAGER
                .requestMatchers("/entretiens/**").hasAnyRole("RH", "MANAGER", "ADMIN")

                // la decision d'embauche : DIRECTION seule
                .requestMatchers("/decisions/**").hasRole("DIRECTION")

                // tout le reste : il faut etre connecte
                .anyRequest().authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll())

            // Authentification HTTP Basic (en-tete "Authorization: Basic ...").
            // Le site utilise surtout le formulaire, mais cette methode permet
            // de tester les droits de chaque role tres simplement, et de
            // preparer de futures API.
            .httpBasic(Customizer.withDefaults())

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))

            .userDetailsService(utilisateurDetailsService)

            // Quand un visiteur n'est pas connecte :
            //  - sur une page HTML (navigateur)  -> redirection vers /login
            //  - sur un appel de type API        -> erreur 401 Unauthorized
            .exceptionHandling(exceptions -> exceptions
                    .defaultAuthenticationEntryPointFor(
                            new LoginUrlAuthenticationEntryPoint("/login"),
                            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    /**
     * BCrypt : le mot de passe n'est JAMAIS stocke en clair.
     * On stocke son empreinte (hash) : impossible de retrouver "midera2026"
     * a partir du hash. C'est exactement ce que contient la colonne
     * mot_de_passe de notre table utilisateur.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
