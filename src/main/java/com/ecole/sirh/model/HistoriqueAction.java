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
 * La table "historique_action" : le journal des actions realisees dans le SIRH.
 *
 * Elle contient 1 cle etrangere :
 *  - utilisateur : l'auteur de l'action (un Utilisateur)
 *
 * "id_entite" n'est pas une cle etrangere : c'est simplement l'identifiant
 * de la ligne concernee dans la table visee (par exemple l'id d'un employe).
 */
@Entity
@Table(name = "historique_action")
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistorique;

    @Column(name = "action_effectuee", nullable = false)
    private String actionEffectuee;

    /** Nom de la table ou de l'entite concernee (exemple : "employe"). */
    @Column(name = "entite_concernee", nullable = false)
    private String entiteConcernee;

    /** Identifiant de la ligne concernee (nullable). */
    @Column(name = "id_entite")
    private Integer idEntite;

    @Column(name = "details")
    private String details;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    public HistoriqueAction() {
    }

    public HistoriqueAction(String actionEffectuee, String entiteConcernee, Integer idEntite,
                            Utilisateur utilisateur) {
        this.actionEffectuee = actionEffectuee;
        this.entiteConcernee = entiteConcernee;
        this.idEntite = idEntite;
        this.utilisateur = utilisateur;
        this.dateAction = LocalDateTime.now();
    }

    public Long getIdHistorique() { return idHistorique; }
    public void setIdHistorique(Long idHistorique) { this.idHistorique = idHistorique; }

    public String getActionEffectuee() { return actionEffectuee; }
    public void setActionEffectuee(String actionEffectuee) { this.actionEffectuee = actionEffectuee; }

    public String getEntiteConcernee() { return entiteConcernee; }
    public void setEntiteConcernee(String entiteConcernee) { this.entiteConcernee = entiteConcernee; }

    public Integer getIdEntite() { return idEntite; }
    public void setIdEntite(Integer idEntite) { this.idEntite = idEntite; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    @Override
    public String toString() {
        return actionEffectuee + " sur " + entiteConcernee;
    }
}
