package com.ecole.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * La table "departement" : les services de l'entreprise.
 * Une entite simple, sans cle etrangere : c'est la table "mère".
 */
@Entity
@Table(name = "departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Long idDepartement;

    @Column(name = "nom_departement", nullable = false, unique = true)
    private String nomDepartement;

    @Column(name = "description_departement")
    private String descriptionDepartement;

    @Column(name = "budget_annuel", nullable = false)
    private BigDecimal budgetAnnuel = BigDecimal.ZERO;

    public Departement() {
    }

    public Departement(String nomDepartement, String descriptionDepartement, BigDecimal budgetAnnuel) {
        this.nomDepartement = nomDepartement;
        this.descriptionDepartement = descriptionDepartement;
        this.budgetAnnuel = budgetAnnuel;
    }

    public Long getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Long idDepartement) { this.idDepartement = idDepartement; }

    public String getNomDepartement() { return nomDepartement; }
    public void setNomDepartement(String nomDepartement) { this.nomDepartement = nomDepartement; }

    public String getDescriptionDepartement() { return descriptionDepartement; }
    public void setDescriptionDepartement(String descriptionDepartement) { this.descriptionDepartement = descriptionDepartement; }

    public BigDecimal getBudgetAnnuel() { return budgetAnnuel; }
    public void setBudgetAnnuel(BigDecimal budgetAnnuel) { this.budgetAnnuel = budgetAnnuel; }

    @Override
    public String toString() {
        return nomDepartement;
    }
}
