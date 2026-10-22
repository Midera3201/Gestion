package com.ecole.sirh.repository;

import com.ecole.sirh.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartementRepository extends JpaRepository<Departement, Long> {

    /** Liste des departements tries par nom. */
    List<Departement> findAllByOrderByNomDepartementAsc();

    boolean existsByNomDepartement(String nomDepartement);
}
