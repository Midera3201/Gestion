package com.ecole.sirh.repository;

import com.ecole.sirh.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    /** Toutes les candidatures, de la plus recente a la plus ancienne. */
    List<Candidature> findAllByOrderByDateCandidatureDesc();

    /** Les candidatures d'une offre, de la plus recente a la plus ancienne. */
    List<Candidature> findByOffre_IdOffreOrderByDateCandidatureDesc(Long idOffre);

    /** La candidature d'un candidat pour une offre donnee. */
    Optional<Candidature> findByCandidat_IdCandidatAndOffre_IdOffre(Long idCandidat, Long idOffre);

    /** Les candidatures ayant un statut donne, de la plus recente a la plus ancienne. */
    List<Candidature> findAllByStatutCandidatureOrderByDateCandidatureDesc(String statut);

    /** Les candidatures d'une offre ayant un statut donne. */
    List<Candidature> findByOffre_IdOffreAndStatutCandidature(Long idOffre, String statut);
}
