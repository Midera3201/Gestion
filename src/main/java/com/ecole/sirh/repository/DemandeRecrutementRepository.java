package com.ecole.sirh.repository;

import com.ecole.sirh.model.DemandeRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemandeRecrutementRepository extends JpaRepository<DemandeRecrutement, Long> {

    /** Les demandes de la plus recente a la plus ancienne. */
    List<DemandeRecrutement> findAllByOrderByIdDemandeDesc();

    /** Un seul statut : sert pour le tableau de bord du MANAGER et de la DIRECTION. */
    List<DemandeRecrutement> findByStatutDemandeOrderByIdDemandeDesc(String statutDemande);

    /** Les demandes creees par un utilisateur (espace perso du MANAGER). */
    List<DemandeRecrutement> findByDemandeur_IdUtilisateurOrderByIdDemandeDesc(Long idUtilisateur);

    /** Sert a generer une reference unique du type DEM-2026-001. */
    Optional<DemandeRecrutement> findByReferenceDemande(String referenceDemande);
}
