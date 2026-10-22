package com.ecole.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ENTITE = une classe Java qui represente une table de la base.
 *
 * @Entity      : dit a Hibernate "cette classe correspond a une table".
 * @Table       : precise le NOM de la table dans PostgreSQL.
 * @Id          : la cle primaire.
 * @GeneratedValue(strategy = IDENTITY) : la valeur est generee par PostgreSQL
 *                (c'est exactement le SERIAL de notre script SQL).
 * @Column      : fait le lien entre l'attribut Java et la colonne SQL.
 *
 * Convention : Java en camelCase (nomDepartement), SQL en snake_case
 * (nom_departement). @Column fait la traduction.
 */
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    @Column(name = "code_role", nullable = false, unique = true)
    private String codeRole;

    @Column(name = "libelle_role", nullable = false)
    private String libelleRole;

    // Constructeur vide : obligatoire pour JPA
    public Role() {
    }

    public Role(String codeRole, String libelleRole) {
        this.codeRole = codeRole;
        this.libelleRole = libelleRole;
    }

    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }

    public String getCodeRole() { return codeRole; }
    public void setCodeRole(String codeRole) { this.codeRole = codeRole; }

    public String getLibelleRole() { return libelleRole; }
    public void setLibelleRole(String libelleRole) { this.libelleRole = libelleRole; }

    @Override
    public String toString() {
        return codeRole;
    }
}
