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
 * La table "offre_emploi" : l'offre d'emploi publiee en interne.
 *
 * Elle contient 1 cle etrangere :
 *  - demande : la demande de recrutement (DemandeRecrutement) a l'origine
 *              de l'offre (relation 1-1 : une demande donne une offre)
 *
 * Le type de contrat et le statut sont de simples textes (String) : c'est la
 * table SQL qui verifie les valeurs autorisees avec une contrainte CHECK.
 */
@Entity
@Table(name = "offre_emploi")
public class OffreEmploi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    @Column(name = "titre_offre", nullable = false)
    private String titreOffre;

    @Column(name = "description_offre", nullable = false)
    private String descriptionOffre;

    /** Lieu de travail (facultatif). */
    @Column(name = "lieu_offre")
    private String lieuOffre;

    /** CDI, CDD, STAGE ou ALTERNANCE. */
    @Column(name = "type_contrat", nullable = false)
    private String typeContrat = "CDI";

    /** Date et heure de publication (facultatif tant que l'offre est brouillon). */
    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    /** Date de fin des candidatures (facultatif). */
    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    /** BROUILLON, PUBLIEE ou CLOTUREE. */
    @Column(name = "statut_offre", nullable = false)
    private String statutOffre = "BROUILLON";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_demande", nullable = false, unique = true)
    private DemandeRecrutement demande;

    public OffreEmploi() {
    }

    public OffreEmploi(String titreOffre, String descriptionOffre, DemandeRecrutement demande) {
        this.titreOffre = titreOffre;
        this.descriptionOffre = descriptionOffre;
        this.demande = demande;
    }

    public Long getIdOffre() { return idOffre; }
    public void setIdOffre(Long idOffre) { this.idOffre = idOffre; }

    public String getTitreOffre() { return titreOffre; }
    public void setTitreOffre(String titreOffre) { this.titreOffre = titreOffre; }

    public String getDescriptionOffre() { return descriptionOffre; }
    public void setDescriptionOffre(String descriptionOffre) { this.descriptionOffre = descriptionOffre; }

    public String getLieuOffre() { return lieuOffre; }
    public void setLieuOffre(String lieuOffre) { this.lieuOffre = lieuOffre; }

    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDate getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDate dateCloture) { this.dateCloture = dateCloture; }

    public String getStatutOffre() { return statutOffre; }
    public void setStatutOffre(String statutOffre) { this.statutOffre = statutOffre; }

    public DemandeRecrutement getDemande() { return demande; }
    public void setDemande(DemandeRecrutement demande) { this.demande = demande; }

    @Override
    public String toString() {
        return titreOffre;
    }
}
