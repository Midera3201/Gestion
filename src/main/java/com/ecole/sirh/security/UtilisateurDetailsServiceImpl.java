package com.ecole.sirh.security;

import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Sert a aller chercher l'utilisateur dans PostgreSQL au moment de la connexion.
 *
 * Spring Security appelle automatiquement loadUserByUsername(email) quand
 * l'utilisateur remplit le formulaire de connexion.
 */
@Service
public class UtilisateurDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurDetailsServiceImpl(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun compte avec l'email " + email));

        // Un compte desactive ne peut pas se connecter.
        if (!utilisateur.isActif()) {
            throw new UsernameNotFoundException("Ce compte a ete desactive.");
        }

        return new UtilisateurPrincipal(utilisateur);
    }
}
