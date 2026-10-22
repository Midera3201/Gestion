package com.ecole.sirh.repository;

import com.ecole.sirh.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Long> {

    /** Les validations d'une demande, de la plus ancienne a la plus recente. */
    List<Validation> findByDemande_IdDemandeOrderByDateDecisionAsc(Long idDemande);

    /** Toutes les validations, de la plus recente a la plus ancienne. */
    List<Validation> findAllByOrderByDateDecisionDesc();

    /** La validation d'une demande pour une etape donnee (RH, FINANCE, DIRECTION). */
    Optional<Validation> findByDemande_IdDemandeAndEtapeValidation(Long idDemande, String etapeValidation);
}
