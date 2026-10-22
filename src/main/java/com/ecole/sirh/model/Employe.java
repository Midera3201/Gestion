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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * La table "employe" : la personne employee apres une decision d'embauche.
 *
 * Elle contient 3 cles etrangeres :
 *  - poste       : le poste occupe
 *  - departement : le service de rattachement
 *  - candidature : la candidature d'origine (nullable : un ancien employe
 *                 reintegration peut avoir ete saisi manuellement)
 *
 * Le statut est un simple texte (String) : ACTIF ou INACTIF.
 * Ce sont les contraintes CHECK de la table SQL qui verifient la valeur.
 */
@Entity
@Table(name = "employe")
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Long idEmploye;

    @Column(name = "matricule", nullable = false, unique = true)
    private String matricule;

    @Column(name = "nom_employe", nullable = false)
    private String nomEmploye;

    @Column(name = "prenom_employe", nullable = false)
    private String prenomEmploye;

    @Column(name = "email_employe", nullable = false)
    private String emailEmploye;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    /** ACTIF ou INACTIF (contrainte CHECK SQL). */
    @Column(name = "statut_employe", nullable = false)
    private String statutEmploye = "ACTIF";

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    /** Colonne nullable : la candidature d'origine n'est pas obligatoire. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;

    public Employe() {
    }

    public Employe(String matricule, String nomEmploye, String prenomEmploye, String emailEmploye,
                   LocalDate dateEmbauche, Poste poste, Departement departement) {
        this.matricule = matricule;
        this.nomEmploye = nomEmploye;
        this.prenomEmploye = prenomEmploye;
        this.emailEmploye = emailEmploye;
        this.dateEmbauche = dateEmbauche;
        this.poste = poste;
        this.departement = departement;
        this.statutEmploye = "ACTIF";
        this.dateCreation = LocalDateTime.now();
    }

    public Long getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Long idEmploye) { this.idEmploye = idEmploye; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNomEmploye() { return nomEmploye; }
    public void setNomEmploye(String nomEmploye) { this.nomEmploye = nomEmploye; }

    public String getPrenomEmploye() { return prenomEmploye; }
    public void setPrenomEmploye(String prenomEmploye) { this.prenomEmploye = prenomEmploye; }

    public String getEmailEmploye() { return emailEmploye; }
    public void setEmailEmploye(String emailEmploye) { this.emailEmploye = emailEmploye; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public String getStatutEmploye() { return statutEmploye; }
    public void setStatutEmploye(String statutEmploye) { this.statutEmploye = statutEmploye; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    /** Nom complet affiche dans le site. */
    public String getNomComplet() {
        return prenomEmploye + " " + nomEmploye;
    }

    @Override
    public String toString() {
        return matricule;
    }
}
