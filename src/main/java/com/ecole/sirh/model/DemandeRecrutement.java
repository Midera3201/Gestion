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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * La table "demande_recrutement" : la demande creee par un MANAGER.
 *
 * Elle contient 3 cles etrangeres :
 *  - poste       : sur quel poste on recrute
 *  - departement : dans quel service
 *  - demandeur   : qui a fait la demande (un Utilisateur)
 *
 * Le statut est un simple texte (String) : c'est la table SQL qui verifie
 * les valeurs autorisees avec une contrainte CHECK.
 * On evite ainsi les enums Java, plus compliques a faire evoluer.
 */
@Entity
@Table(name = "demande_recrutement")
public class DemandeRecrutement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Long idDemande;

    @Column(name = "reference_demande", nullable = false, unique = true)
    private String referenceDemande;

    @Column(name = "motif_demande", nullable = false)
    private String motifDemande;

    @Column(name = "nombre_postes", nullable = false)
    private int nombrePostes = 1;

    @Column(name = "budget_estime", nullable = false)
    private BigDecimal budgetEstime = BigDecimal.ZERO;

    @Column(name = "date_besoin", nullable = false)
    private LocalDate dateBesoin;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "statut_demande", nullable = false)
    private String statutDemande = "EN_ATTENTE_RH";

    @Column(name = "finance_requise", nullable = false)
    private boolean financeRequise = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Utilisateur demandeur;

    public DemandeRecrutement() {
    }

    public DemandeRecrutement(String referenceDemande, String motifDemande, Poste poste,
                              Departement departement, Utilisateur demandeur) {
        this.referenceDemande = referenceDemande;
        this.motifDemande = motifDemande;
        this.poste = poste;
        this.departement = departement;
        this.demandeur = demandeur;
    }

    public Long getIdDemande() { return idDemande; }
    public void setIdDemande(Long idDemande) { this.idDemande = idDemande; }

    public String getReferenceDemande() { return referenceDemande; }
    public void setReferenceDemande(String referenceDemande) { this.referenceDemande = referenceDemande; }

    public String getMotifDemande() { return motifDemande; }
    public void setMotifDemande(String motifDemande) { this.motifDemande = motifDemande; }

    public int getNombrePostes() { return nombrePostes; }
    public void setNombrePostes(int nombrePostes) { this.nombrePostes = nombrePostes; }

    public BigDecimal getBudgetEstime() { return budgetEstime; }
    public void setBudgetEstime(BigDecimal budgetEstime) { this.budgetEstime = budgetEstime; }

    public LocalDate getDateBesoin() { return dateBesoin; }
    public void setDateBesoin(LocalDate dateBesoin) { this.dateBesoin = dateBesoin; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getStatutDemande() { return statutDemande; }
    public void setStatutDemande(String statutDemande) { this.statutDemande = statutDemande; }

    public boolean isFinanceRequise() { return financeRequise; }
    public void setFinanceRequise(boolean financeRequise) { this.financeRequise = financeRequise; }

    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public Utilisateur getDemandeur() { return demandeur; }
    public void setDemandeur(Utilisateur demandeur) { this.demandeur = demandeur; }

    @Override
    public String toString() {
        return referenceDemande;
    }
}
