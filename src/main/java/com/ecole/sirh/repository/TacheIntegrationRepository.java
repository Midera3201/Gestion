package com.ecole.sirh.repository;

import com.ecole.sirh.model.TacheIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheIntegrationRepository extends JpaRepository<TacheIntegration, Long> {

    /** Les taches d'integration d'un employe, de la plus proche a la plus eloignee. */
    List<TacheIntegration> findByEmploye_IdEmployeOrderByDatePrevueAsc(Long idEmploye);

    /** Toutes les taches, de la plus proche a la plus eloignee. */
    List<TacheIntegration> findAllByOrderByDatePrevueAsc();

    /** Les taches ayant un statut donne (A_FAIRE, EN_COURS ou TERMINEE). */
    List<TacheIntegration> findAllByStatutTacheOrderByDatePrevueAsc(String statut);
}
