package com.ecole.sirh.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Petits utilitaires d'affichage, utilises par les pages Thymeleaf.
 *
 * On y accede dans une page avec :  ${@affichage.badge('ACCEPTEE')}
 * (le "nom du bean" commence par une lettre minuscule).
 *
 * Cela evite d'ecrire la meme longue condition Thymeleaf dans chaque
 * tableau du site.
 */
@Component("affichage")
public class Affichage {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * La couleur du badge d'un statut.
     * Le statut est un code technique (EN_ATTENTE_RH) : on le colore
     * pour que le tableau se lise d'un coup d'oeil.
     */
    public String badge(String statut) {
        if (statut == null) {
            return "bg-secondary";
        }
        switch (statut) {
            case "ACCEPTEE":
            case "APPROUVEE":
            case "PUBLIEE":
            case "EMBAUCHEE":
            case "OFFRE_ACCEPTEE":
            case "SIGNE":
            case "TERMINEE":
            case "FAVORABLE":
            case "ACTIF":
                return "bg-success";
            case "REFUSEE":
            case "ANNULEE":
            case "DEFAVORABLE":
            case "INACTIF":
                return "bg-danger";
            case "EN_ATTENTE_RH":
            case "EN_ATTENTE_FINANCE":
            case "EN_ATTENTE_DIRECTION":
            case "RECUE":
            case "PLANIFIE":
            case "BROUILLON":
            case "A_FAIRE":
            case "RESERVE":
                return "bg-warning text-dark";
            case "EN_ENTRETIEN":
            case "EVALUEE":
            case "RETENUE":
            case "OFFRE_ENVOYEE":
            case "EN_COURS":
                return "bg-info text-dark";
            default:
                return "bg-secondary";
        }
    }

    /** Un libelle lisible pour un statut ou un code. */
    public String libelle(String code) {
        if (code == null) {
            return "-";
        }
        switch (code) {
            case "EN_ATTENTE_RH":
                return "En attente du RH";
            case "EN_ATTENTE_FINANCE":
                return "En attente de la Finance";
            case "EN_ATTENTE_DIRECTION":
                return "En attente de la Direction";
            case "ACCEPTEE":
                return "Acceptee";
            case "REFUSEE":
                return "Refusee";
            case "ANNULEE":
                return "Annulee";
            case "RECUE":
                return "Recue";
            case "EN_ENTRETIEN":
                return "En entretien";
            case "EVALUEE":
                return "Evaluee";
            case "RETENUE":
                return "Retenue";
            case "OFFRE_ENVOYEE":
                return "Offre envoyee";
            case "OFFRE_ACCEPTEE":
                return "Offre acceptee";
            case "EMBAUCHEE":
                return "Embauchee";
            case "PUBLIEE":
                return "Publiee";
            case "CLOTUREE":
                return "Cloturee";
            case "BROUILLON":
                return "Brouillon";
            case "VALIDEE":
                return "Validee";
            case "PLANIFIE":
                return "Planifie";
            case "REALISE":
                return "Realise";
            case "FAVORABLE":
                return "Favorable";
            case "DEFAVORABLE":
                return "Defavorable";
            case "RESERVE":
                return "Reserve";
            case "A_FAIRE":
                return "A faire";
            case "EN_COURS":
                return "En cours";
            case "TERMINEE":
                return "Terminee";
            case "SIGNE":
                return "Signe";
            case "ACTIF":
                return "Actif";
            case "INACTIF":
                return "Inactif";
            default:
                return code;
        }
    }

    /** Une date formatee (dd/MM/yyyy), ou une chaine vide si la date est nulle. */
    public String date(Object valeur) {
        if (valeur instanceof LocalDate d) {
            return d.format(DATE);
        }
        if (valeur instanceof LocalDateTime d) {
            return d.format(DATE);
        }
        return "";
    }

    /** Une date et une heure formatees (dd/MM/yyyy HH:mm). */
    public String dateHeure(Object valeur) {
        if (valeur instanceof LocalDateTime d) {
            return d.format(DATE_HEURE);
        }
        if (valeur instanceof LocalDate d) {
            return d.format(DATE);
        }
        return "";
    }

    /** Un montant en euros : 46 000,00 EUR */
    public String argent(BigDecimal montant) {
        if (montant == null) {
            return "-";
        }
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(montant) + " EUR";
    }
}
