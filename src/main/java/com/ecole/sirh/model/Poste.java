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

/**
 * La table "poste" : une fonction (Developpeur Java, Comptable...).
 * Chaque poste appartient a UN departement.
 */
@Entity
@Table(name = "poste")
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poste")
    private Long idPoste;

    @Column(name = "titre_poste", nullable = false)
    private String titrePoste;

    @Column(name = "description_poste")
    private String descriptionPoste;

    @Column(name = "salaire_min")
    private BigDecimal salaireMin;

    @Column(name = "salaire_max")
    private BigDecimal salaireMax;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    public Poste() {
    }

    public Poste(String titrePoste, Departement departement) {
        this.titrePoste = titrePoste;
        this.departement = departement;
    }

    public Long getIdPoste() { return idPoste; }
    public void setIdPoste(Long idPoste) { this.idPoste = idPoste; }

    public String getTitrePoste() { return titrePoste; }
    public void setTitrePoste(String titrePoste) { this.titrePoste = titrePoste; }

    public String getDescriptionPoste() { return descriptionPoste; }
    public void setDescriptionPoste(String descriptionPoste) { this.descriptionPoste = descriptionPoste; }

    public BigDecimal getSalaireMin() { return salaireMin; }
    public void setSalaireMin(BigDecimal salaireMin) { this.salaireMin = salaireMin; }

    public BigDecimal getSalaireMax() { return salaireMax; }
    public void setSalaireMax(BigDecimal salaireMax) { this.salaireMax = salaireMax; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    @Override
    public String toString() {
        return titrePoste;
    }
}
