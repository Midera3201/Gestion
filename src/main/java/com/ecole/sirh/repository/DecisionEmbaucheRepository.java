package com.ecole.sirh.repository;

import com.ecole.sirh.model.DecisionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DecisionEmbaucheRepository extends JpaRepository<DecisionEmbauche, Long> {

    /** La decision d'une candidature (il n'y en a qu'une seule par candidature). */
    Optional<DecisionEmbauche> findByCandidature_IdCandidature(Long idCandidature);

    /** Toutes les decisions, de la plus recente a la plus ancienne. */
    List<DecisionEmbauche> findAllByOrderByDateDecisionDesc();
}
