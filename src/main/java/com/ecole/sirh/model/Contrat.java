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

/**
 * La table "contrat" : le contrat de travail d'un employe.
 *
 * Elle contient 1 cle etrangere :
 *  - employe : l'employe concerne (supprime en cascade avec lui)
 *
 * Le type et le statut sont de simples textes (String) :
 * ce sont les contraintes CHECK de la table SQL qui verifient les valeurs.
 */
@Entity
@Table(name = "contrat")
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Long idContrat;

    /** CDI, CDD, STAGE ou ALTERNANCE (contrainte CHECK SQL). */
    @Column(name = "type_contrat", nullable = false)
    private String typeContrat;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /** Nullable : un CDI n'a pas de date de fin. */
    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "salaire", nullable = false)
    private BigDecimal salaire;

    /** BROUILLON, SIGNE ou TERMINE (contrainte CHECK SQL). */
    @Column(name = "statut_contrat", nullable = false)
    private String statutContrat = "BROUILLON";

    /** Nullable : le contrat peut ne pas etre signe. */
    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;

    public Contrat() {
    }

    public Contrat(String typeContrat, LocalDate dateDebut, BigDecimal salaire, Employe employe) {
        this.typeContrat = typeContrat;
        this.dateDebut = dateDebut;
        this.salaire = salaire;
        this.employe = employe;
        this.statutContrat = "BROUILLON";
    }

    public Long getIdContrat() { return idContrat; }
    public void setIdContrat(Long idContrat) { this.idContrat = idContrat; }

    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }

    public String getStatutContrat() { return statutContrat; }
    public void setStatutContrat(String statutContrat) { this.statutContrat = statutContrat; }

    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    @Override
    public String toString() {
        return typeContrat;
    }
}
