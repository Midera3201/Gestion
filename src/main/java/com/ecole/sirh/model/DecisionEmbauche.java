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
 * La table "decision_embauche" : la decision finale pour une candidature.
 *
 * Elle contient 2 cles etrangeres :
 *  - candidature : la candidature concernee (une seule decision par candidature)
 *  - decideur    : l'utilisateur qui a tranche (un Utilisateur)
 *
 * La decision est un simple texte (String) : APPROUVEE ou REFUSEE.
 * Ce sont les contraintes CHECK de la table SQL qui verifient la valeur.
 */
@Entity
@Table(name = "decision_embauche")
public class DecisionEmbauche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decision")
    private Long idDecision;

    /** APPROUVEE ou REFUSEE (contrainte CHECK SQL). */
    @Column(name = "decision_finale", nullable = false)
    private String decisionFinale;

    @Column(name = "commentaire_decision")
    private String commentaireDecision;

    @Column(name = "date_decision", nullable = false)
    private LocalDateTime dateDecision = LocalDateTime.now();

    /** Une seule decision par candidature (contrainte UNIQUE SQL). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_candidature", nullable = false, unique = true)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_decideur", nullable = false)
    private Utilisateur decideur;

    public DecisionEmbauche() {
    }

    public DecisionEmbauche(String decisionFinale, Candidature candidature, Utilisateur decideur) {
        this.decisionFinale = decisionFinale;
        this.candidature = candidature;
        this.decideur = decideur;
        this.dateDecision = LocalDateTime.now();
    }

    public Long getIdDecision() { return idDecision; }
    public void setIdDecision(Long idDecision) { this.idDecision = idDecision; }

    public String getDecisionFinale() { return decisionFinale; }
    public void setDecisionFinale(String decisionFinale) { this.decisionFinale = decisionFinale; }

    public String getCommentaireDecision() { return commentaireDecision; }
    public void setCommentaireDecision(String commentaireDecision) { this.commentaireDecision = commentaireDecision; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public Utilisateur getDecideur() { return decideur; }
    public void setDecideur(Utilisateur decideur) { this.decideur = decideur; }

    @Override
    public String toString() {
        return decisionFinale;
    }
}
