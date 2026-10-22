package com.ecole.sirh.repository;

import com.ecole.sirh.model.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosteRepository extends JpaRepository<Poste, Long> {

    /** Les postes d'un departement : findBy + le nom de l'attribut lie a la FK. */
    List<Poste> findByDepartement_IdDepartementOrderByTitrePosteAsc(Long idDepartement);

    List<Poste> findAllByOrderByTitrePosteAsc();
}
