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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * La table "candidature" : la candidature d'un candidat a une offre d'emploi.
 *
 * Elle contient 2 cles etrangeres :
 *  - candidat : le candidat (Candidat) qui postule
 *  - offre    : l'offre (OffreEmploi) visee
 *
 * Un candidat ne peut postuler qu'une seule fois a la meme offre
 * (contrainte unique sur id_candidat + id_offre).
 *
 * Le statut est un simple texte (String) : c'est la table SQL qui verifie
 * les valeurs autorisees avec une contrainte CHECK.
 */
@Entity
@Table(name = "candidature", uniqueConstraints = {
        @UniqueConstraint(name = "uq_candidature", columnNames = {"id_candidat", "id_offre"})
})
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Long idCandidature;

    /** Date et heure de depot de la candidature. */
    @Column(name = "date_candidature", nullable = false)
    private LocalDateTime dateCandidature = LocalDateTime.now();

    /** RECUE, EN_ENTRETIEN, EVALUEE, RETENUE, REFUSEE, ... */
    @Column(name = "statut_candidature", nullable = false)
    private String statutCandidature = "RECUE";

    /** Lettre de motivation (facultatif). */
    @Column(name = "lettre_motivation")
    private String lettreMotivation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_offre", nullable = false)
    private OffreEmploi offre;

    public Candidature() {
    }

    public Candidature(Candidat candidat, OffreEmploi offre) {
        this.candidat = candidat;
        this.offre = offre;
    }

    public Long getIdCandidature() { return idCandidature; }
    public void setIdCandidature(Long idCandidature) { this.idCandidature = idCandidature; }

    public LocalDateTime getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDateTime dateCandidature) { this.dateCandidature = dateCandidature; }

    public String getStatutCandidature() { return statutCandidature; }
    public void setStatutCandidature(String statutCandidature) { this.statutCandidature = statutCandidature; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public OffreEmploi getOffre() { return offre; }
    public void setOffre(OffreEmploi offre) { this.offre = offre; }

    @Override
    public String toString() {
        return "Candidature " + idCandidature;
    }
}
