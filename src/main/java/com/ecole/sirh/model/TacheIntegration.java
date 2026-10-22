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

/**
 * La table "tache_integration" : une tache du plan d'integration d'un nouvel employe.
 *
 * Elle contient 2 cles etrangeres :
 *  - employe    : l'employe concerne (supprime en cascade avec lui)
 *  - responsable : l'utilisateur qui suit la tache (un Utilisateur)
 *
 * Le statut est un simple texte (String) : A_FAIRE, EN_COURS ou TERMINEE.
 * Ce sont les contraintes CHECK de la table SQL qui verifient la valeur.
 */
@Entity
@Table(name = "tache_integration")
public class TacheIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tache")
    private Long idTache;

    @Column(name = "libelle_tache", nullable = false)
    private String libelleTache;

    @Column(name = "date_prevue")
    private LocalDate datePrevue;

    /** Nullable : la tache peut pas encore etre realisee. */
    @Column(name = "date_realisation")
    private LocalDate dateRealisation;

    /** A_FAIRE, EN_COURS ou TERMINEE (contrainte CHECK SQL). */
    @Column(name = "statut_tache", nullable = false)
    private String statutTache = "A_FAIRE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Utilisateur responsable;

    public TacheIntegration() {
    }

    public TacheIntegration(String libelleTache, LocalDate datePrevue, Employe employe,
                             Utilisateur responsable) {
        this.libelleTache = libelleTache;
        this.datePrevue = datePrevue;
        this.employe = employe;
        this.responsable = responsable;
        this.statutTache = "A_FAIRE";
    }

    public Long getIdTache() { return idTache; }
    public void setIdTache(Long idTache) { this.idTache = idTache; }

    public String getLibelleTache() { return libelleTache; }
    public void setLibelleTache(String libelleTache) { this.libelleTache = libelleTache; }

    public LocalDate getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDate datePrevue) { this.datePrevue = datePrevue; }

    public LocalDate getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDate dateRealisation) { this.dateRealisation = dateRealisation; }

    public String getStatutTache() { return statutTache; }
    public void setStatutTache(String statutTache) { this.statutTache = statutTache; }

    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }

    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }

    @Override
    public String toString() {
        return libelleTache;
    }
}
