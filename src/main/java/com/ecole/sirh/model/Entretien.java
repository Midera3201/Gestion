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
 * La table "entretien" : un entretien planifie pour une candidature.
 *
 * Elle contient 1 cle etrangere :
 *  - candidature : la candidature (Candidature) concernee par l'entretien
 *
 * Le type et le statut sont de simples textes (String) : c'est la table SQL
 * qui verifie les valeurs autorisees avec une contrainte CHECK.
 */
@Entity
@Table(name = "entretien")
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entretien")
    private Long idEntretien;

    /** Date et heure de l'entretien. */
    @Column(name = "date_heure_entretien", nullable = false)
    private LocalDateTime dateHeureEntretien;

    /** Lieu de l'entretien (facultatif). */
    @Column(name = "lieu_entretien")
    private String lieuEntretien;

    /** PRESENTIEL, VISIO ou TELEPHONE. */
    @Column(name = "type_entretien", nullable = false)
    private String typeEntretien = "PRESENTIEL";

    /** PLANIFIE, REALISE ou ANNULE. */
    @Column(name = "statut_entretien", nullable = false)
    private String statutEntretien = "PLANIFIE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    public Entretien() {
    }

    public Entretien(LocalDateTime dateHeureEntretien, Candidature candidature) {
        this.dateHeureEntretien = dateHeureEntretien;
        this.candidature = candidature;
    }

    public Long getIdEntretien() { return idEntretien; }
    public void setIdEntretien(Long idEntretien) { this.idEntretien = idEntretien; }

    public LocalDateTime getDateHeureEntretien() { return dateHeureEntretien; }
    public void setDateHeureEntretien(LocalDateTime dateHeureEntretien) { this.dateHeureEntretien = dateHeureEntretien; }

    public String getLieuEntretien() { return lieuEntretien; }
    public void setLieuEntretien(String lieuEntretien) { this.lieuEntretien = lieuEntretien; }

    public String getTypeEntretien() { return typeEntretien; }
    public void setTypeEntretien(String typeEntretien) { this.typeEntretien = typeEntretien; }

    public String getStatutEntretien() { return statutEntretien; }
    public void setStatutEntretien(String statutEntretien) { this.statutEntretien = statutEntretien; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    @Override
    public String toString() {
        return "Entretien " + idEntretien;
    }
}
