package com.ecole.sirh.repository;

import com.ecole.sirh.model.HistoriqueAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueActionRepository extends JpaRepository<HistoriqueAction, Long> {

    /** Tout l'historique, de l'action la plus recente a la plus ancienne. */
    List<HistoriqueAction> findAllByOrderByDateActionDesc();

    /** Les 50 dernieres actions seulement (bandeau du tableau de bord). */
    List<HistoriqueAction> findTop50ByOrderByDateActionDesc();

    /** Les actions concernees par une entite donnee, de la plus recente a la plus ancienne. */
    List<HistoriqueAction> findByEntiteConcerneeAndIdEntiteOrderByDateActionDesc(String entiteConcernee, Integer idEntite);
}
