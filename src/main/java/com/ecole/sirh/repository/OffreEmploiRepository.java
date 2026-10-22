package com.ecole.sirh.repository;

import com.ecole.sirh.model.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {

    /** Toutes les offres, de la plus recente a la plus ancienne. */
    List<OffreEmploi> findAllByOrderByIdOffreDesc();

    /** Les offres ayant un statut donne (BROUILLON, PUBLIEE, CLOTUREE). */
    List<OffreEmploi> findAllByStatutOffreOrderByIdOffreDesc(String statutOffre);

    /** L'offre issue d'une demande de recrutement donnee. */
    Optional<OffreEmploi> findByDemande_IdDemande(Long idDemande);

    /** Les offres ayant un des statuts donnes, de la publication la plus recente a la plus ancienne. */
    List<OffreEmploi> findAllByStatutOffreInOrderByDatePublicationDesc(List<String> statuts);
}
