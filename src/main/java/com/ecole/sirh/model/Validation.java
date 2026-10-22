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
 * La table "validation" : une decision de validation a une etape du circuit
 * de recrutement (RH, FINANCE ou DIRECTION).
 *
 * Elle contient 2 cles etrangeres :
 *  - demande  : la demande de recrutement (DemandeRecrutement) qui est validee
 *  - valideur : l'utilisateur (Utilisateur) qui a rendu la decision
 *
 * L'etape et la decision sont de simples textes (String) : c'est la table SQL
 * qui verifie les valeurs autorisees avec une contrainte CHECK.
 * On evite ainsi les enums Java, plus compliques a faire evoluer.
 */
@Entity
@Table(name = "validation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_validation_demande_etape", columnNames = {"id_demande", "etape_validation"})
})
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validation")
    private Long idValidation;

    /** Etape du circuit : RH, FINANCE ou DIRECTION. */
    @Column(name = "etape_validation", nullable = false)
    private String etapeValidation;

    /** Decision : VALIDEE ou REFUSEE. */
    @Column(name = "decision_validation", nullable = false)
    private String decisionValidation;

    /** Commentaire libre du valideur (facultatif). */
    @Column(name = "commentaire_validation")
    private String commentaireValidation;

    /** Date et heure de la decision. */
    @Column(name = "date_decision", nullable = false)
    private LocalDateTime dateDecision = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_demande", nullable = false)
    private DemandeRecrutement demande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_valideur", nullable = false)
    private Utilisateur valideur;

    public Validation() {
    }

    public Validation(String etapeValidation, String decisionValidation,
                      DemandeRecrutement demande, Utilisateur valideur) {
        this.etapeValidation = etapeValidation;
        this.decisionValidation = decisionValidation;
        this.demande = demande;
        this.valideur = valideur;
    }

    public Long getIdValidation() { return idValidation; }
    public void setIdValidation(Long idValidation) { this.idValidation = idValidation; }

    public String getEtapeValidation() { return etapeValidation; }
    public void setEtapeValidation(String etapeValidation) { this.etapeValidation = etapeValidation; }

    public String getDecisionValidation() { return decisionValidation; }
    public void setDecisionValidation(String decisionValidation) { this.decisionValidation = decisionValidation; }

    public String getCommentaireValidation() { return commentaireValidation; }
    public void setCommentaireValidation(String commentaireValidation) { this.commentaireValidation = commentaireValidation; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public DemandeRecrutement getDemande() { return demande; }
    public void setDemande(DemandeRecrutement demande) { this.demande = demande; }

    public Utilisateur getValideur() { return valideur; }
    public void setValideur(Utilisateur valideur) { this.valideur = valideur; }

    @Override
    public String toString() {
        return etapeValidation + " - " + decisionValidation;
    }
}
