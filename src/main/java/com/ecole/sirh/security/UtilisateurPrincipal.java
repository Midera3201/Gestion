package com.ecole.sirh.security;

import com.ecole.sirh.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * L'objet "connecte" que Spring Security place dans la session.
 *
 * On ne met pas l'entite Utilisateur directement, car UserDetails est
 * l'interface attendue par Spring Security. On y ajoute le nom complet
 * et le code du role, tres utiles pour le menu et le tableau de bord.
 */
public class UtilisateurPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String motDePasse;
    private final String nomComplet;
    private final String codeRole;
    private final boolean actif;
    private final Collection<GrantedAuthority> authorities;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.id = utilisateur.getIdUtilisateur();
        this.email = utilisateur.getEmailUtilisateur();
        this.motDePasse = utilisateur.getMotDePasse();
        this.nomComplet = utilisateur.getNomComplet();
        this.codeRole = utilisateur.getRole().getCodeRole();
        this.actif = utilisateur.isActif();
        // "ROLE_" + code : Spring Security utilise le prefixe ROLE_ par defaut.
        // hasRole("RH") dans la configuration correspond donc a "ROLE_RH".
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + codeRole));
    }

    public Long getId() { return id; }

    public String getNomComplet() { return nomComplet; }

    public String getCodeRole() { return codeRole; }

    public boolean isActif() { return actif; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
