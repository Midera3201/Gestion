package com.ecole.sirh.repository;

import com.ecole.sirh.model.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {

    /** Le candidat correspondant a un email donne. */
    Optional<Candidat> findByEmailCandidat(String emailCandidat);

    /** Tous les candidats, tries par nom puis par prenom. */
    List<Candidat> findAllByOrderByNomCandidatAsc();

    /** Indique si un candidat existe deja avec cet email. */
    boolean existsByEmailCandidat(String emailCandidat);
}
