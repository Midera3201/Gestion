package com.ecole.sirh.repository;

import com.ecole.sirh.model.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntretienRepository extends JpaRepository<Entretien, Long> {

    /** Tous les entretiens, du plus recent au plus ancien. */
    List<Entretien> findAllByOrderByDateHeureEntretienDesc();

    /** Les entretiens d'une candidature, du plus recent au plus ancien. */
    List<Entretien> findByCandidature_IdCandidatureOrderByDateHeureEntretienDesc(Long idCandidature);

    /** Les entretiens ayant un statut donne (PLANIFIE, REALISE, ANNULE), du plus ancien au plus recent. */
    List<Entretien> findAllByStatutEntretienOrderByDateHeureEntretienAsc(String statut);
}
