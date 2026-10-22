package com.ecole.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * La table "candidat" : la personne qui postule aux offres.
 *
 * Cette table ne contient aucune cle etrangere : un candidat existe seul,
 * avant meme de postuler a une offre.
 * Les candidatures sont portees par la table "candidature".
 */
@Entity
@Table(name = "candidat")
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidat")
    private Long idCandidat;

    @Column(name = "nom_candidat", nullable = false)
    private String nomCandidat;

    @Column(name = "prenom_candidat", nullable = false)
    private String prenomCandidat;

    /** Adresse email : elle identifie le candidat, elle est donc unique. */
    @Column(name = "email_candidat", nullable = false, unique = true)
    private String emailCandidat;

    /** Numero de telephone (facultatif). */
    @Column(name = "telephone_candidat")
    private String telephoneCandidat;

    /** Chemin du fichier CV (facultatif). */
    @Column(name = "cv_candidat")
    private String cvCandidat;

    /** Date et heure de creation du compte candidat. */
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Candidat() {
    }

    public Candidat(String nomCandidat, String prenomCandidat, String emailCandidat) {
        this.nomCandidat = nomCandidat;
        this.prenomCandidat = prenomCandidat;
        this.emailCandidat = emailCandidat;
    }

    public Long getIdCandidat() { return idCandidat; }
    public void setIdCandidat(Long idCandidat) { this.idCandidat = idCandidat; }

    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }

    public String getPrenomCandidat() { return prenomCandidat; }
    public void setPrenomCandidat(String prenomCandidat) { this.prenomCandidat = prenomCandidat; }

    public String getEmailCandidat() { return emailCandidat; }
    public void setEmailCandidat(String emailCandidat) { this.emailCandidat = emailCandidat; }

    public String getTelephoneCandidat() { return telephoneCandidat; }
    public void setTelephoneCandidat(String telephoneCandidat) { this.telephoneCandidat = telephoneCandidat; }

    public String getCvCandidat() { return cvCandidat; }
    public void setCvCandidat(String cvCandidat) { this.cvCandidat = cvCandidat; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return prenomCandidat + " " + nomCandidat;
    }
}
