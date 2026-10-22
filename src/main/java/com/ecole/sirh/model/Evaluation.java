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
 * La table "evaluation" : l'avis d'un evaluateur sur un entretien.
 *
 * Elle contient 2 cles etrangeres :
 *  - entretien  : l'entretien qui est evalue
 *  - evaluateur : l'utilisateur qui a rendu l'avis (un Utilisateur)
 *
 * La note est un entier de 1 a 5 et l'avis est un simple texte (String) :
 * c'est la table SQL qui verifie les valeurs autorisees avec une
 * contrainte CHECK. On evite ainsi les enums Java.
 */
@Entity
@Table(name = "evaluation")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation")
    private Long idEvaluation;

    /** Note de 1 a 5 (verifie par la contrainte CHECK de la table SQL). */
    @Column(name = "note_evaluation", nullable = false)
    private int noteEvaluation;

    @Column(name = "points_forts")
    private String pointsForts;

    @Column(name = "points_faibles")
    private String pointsFaibles;

    /** FAVORABLE, DEFAVORABLE ou RESERVE (contrainte CHECK SQL). */
    @Column(name = "avis_evaluation", nullable = false)
    private String avisEvaluation;

    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_entretien", nullable = false)
    private Entretien entretien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_evaluateur", nullable = false)
    private Utilisateur evaluateur;

    public Evaluation() {
    }

    public Evaluation(int noteEvaluation, String avisEvaluation, Entretien entretien,
                      Utilisateur evaluateur) {
        this.noteEvaluation = noteEvaluation;
        this.avisEvaluation = avisEvaluation;
        this.entretien = entretien;
        this.evaluateur = evaluateur;
        this.dateEvaluation = LocalDateTime.now();
    }

    public Long getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(Long idEvaluation) { this.idEvaluation = idEvaluation; }

    public int getNoteEvaluation() { return noteEvaluation; }
    public void setNoteEvaluation(int noteEvaluation) { this.noteEvaluation = noteEvaluation; }

    public String getPointsForts() { return pointsForts; }
    public void setPointsForts(String pointsForts) { this.pointsForts = pointsForts; }

    public String getPointsFaibles() { return pointsFaibles; }
    public void setPointsFaibles(String pointsFaibles) { this.pointsFaibles = pointsFaibles; }

    public String getAvisEvaluation() { return avisEvaluation; }
    public void setAvisEvaluation(String avisEvaluation) { this.avisEvaluation = avisEvaluation; }

    public LocalDateTime getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDateTime dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    public Entretien getEntretien() { return entretien; }
    public void setEntretien(Entretien entretien) { this.entretien = entretien; }

    public Utilisateur getEvaluateur() { return evaluateur; }
    public void setEvaluateur(Utilisateur evaluateur) { this.evaluateur = evaluateur; }

    @Override
    public String toString() {
        return avisEvaluation + " (" + noteEvaluation + "/5)";
    }
}
