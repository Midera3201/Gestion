package com.ecole.sirh.repository;

import com.ecole.sirh.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    /** Toutes les evaluations, de la plus recente a la plus ancienne. */
    List<Evaluation> findAllByOrderByDateEvaluationDesc();

    /** Les evaluations d'un entretien, de la plus ancienne a la plus recente. */
    List<Evaluation> findByEntretien_IdEntretienOrderByDateEvaluationAsc(Long idEntretien);

    /** Vrai si cet evaluateur a deja note cet entretien (contrainte UNIQUE SQL). */
    boolean existsByEntretien_IdEntretienAndEvaluateur_IdUtilisateur(Long idEntretien, Long idEvaluateur);

    /** Les evaluations rendues par un evaluateur, de la plus recente a la plus ancienne. */
    List<Evaluation> findByEvaluateur_IdUtilisateurOrderByDateEvaluationDesc(Long idEvaluateur);
}
