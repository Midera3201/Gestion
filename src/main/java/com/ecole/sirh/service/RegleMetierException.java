package com.ecole.sirh.service;

/**
 * Exception "metier" : elle dit qu'une regle de gestion n'est pas respectee.
 *
 * Exemple : on ne peut pas valider l'etape FINANCE d'une demande qui
 * attend encore l'etape RH.
 *
 * Elle herite de RuntimeException : on n'est pas oblige de la declarer
 * dans les methodes qui la lancent.
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}
