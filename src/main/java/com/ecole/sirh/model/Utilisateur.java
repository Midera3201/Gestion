package com.ecole.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * La table "utilisateur" : les comptes qui se connectent au site.
 *
 * @ManyToOne : la cle etrangere. Un utilisateur a UN role, un role a
 *              PLUSIEURS utilisateurs (relation 1-N, donc @ManyToOne ici).
 * @JoinColumn(name = "id_role") : precise la colonne SQL qui porte la FK.
 */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    @Column(name = "nom_utilisateur", nullable = false)
    private String nomUtilisateur;

    @Column(name = "prenom_utilisateur", nullable = false)
    private String prenomUtilisateur;

    @Column(name = "email_utilisateur", nullable = false, unique = true)
    private String emailUtilisateur;

    /** Hash BCrypt du mot de passe. Jamais le mot de passe en clair. */
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    /**
     * EAGER : la colonne id_role est chargee avec l'utilisateur.
     * On met EAGER parce que la table "role" ne contient que 5 lignes :
     * ce n'est pas un cout, et cela evite les erreurs de lazy loading.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    public Utilisateur() {
    }

    public Utilisateur(String nomUtilisateur, String prenomUtilisateur, String emailUtilisateur,
                       String motDePasse, Role role) {
        this.nomUtilisateur = nomUtilisateur;
        this.prenomUtilisateur = prenomUtilisateur;
        this.emailUtilisateur = emailUtilisateur;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public Long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Long idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getPrenomUtilisateur() { return prenomUtilisateur; }
    public void setPrenomUtilisateur(String prenomUtilisateur) { this.prenomUtilisateur = prenomUtilisateur; }

    public String getEmailUtilisateur() { return emailUtilisateur; }
    public void setEmailUtilisateur(String emailUtilisateur) { this.emailUtilisateur = emailUtilisateur; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    /** Nom complet affiche dans le site. */
    public String getNomComplet() {
        return prenomUtilisateur + " " + nomUtilisateur;
    }

    @Override
    public String toString() {
        return emailUtilisateur;
    }
}
