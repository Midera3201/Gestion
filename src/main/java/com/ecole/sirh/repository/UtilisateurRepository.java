package com.ecole.sirh.repository;

import com.ecole.sirh.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /** Utilise par la page de connexion (etape 5). */
    Optional<Utilisateur> findByEmailUtilisateur(String emailUtilisateur);

    boolean existsByEmailUtilisateur(String emailUtilisateur);

    List<Utilisateur> findByActifTrueOrderByNomUtilisateurAsc();
}
