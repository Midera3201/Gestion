package com.ecole.sirh.repository;

import com.ecole.sirh.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

    /** Tous les employes, tries par nom. */
    List<Employe> findAllByOrderByNomEmployeAsc();

    /** Un employe a partir de son matricule (unique). */
    Optional<Employe> findByMatricule(String matricule);

    /** Les employes ayant un statut donne (ACTIF ou INACTIF), tries par nom. */
    List<Employe> findAllByStatutEmployeOrderByNomEmployeAsc(String statut);

    /** L employe cree a partir d'une candidature donnee. */
    Optional<Employe> findByCandidature_IdCandidature(Long idCandidature);
}
