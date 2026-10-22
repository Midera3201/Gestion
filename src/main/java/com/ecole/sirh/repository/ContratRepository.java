package com.ecole.sirh.repository;

import com.ecole.sirh.model.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    /** Tous les contrats, du plus recent au plus ancien (date de debut). */
    List<Contrat> findAllByOrderByDateDebutDesc();

    /** Les contrats d'un employe, du plus recent au plus ancien. */
    List<Contrat> findByEmploye_IdEmployeOrderByDateDebutDesc(Long idEmploye);
}
