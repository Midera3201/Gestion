package com.ecole.sirh.repository;

import com.ecole.sirh.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Un Repository = l'interface qui donne acces a la table.
 * Spring Data genere tout seul le SQL a partir du NOM de la methode.
 *
 *   findAll()                 -> SELECT * FROM role
 *   findById(1)               -> SELECT ... WHERE id_role = 1
 *   findByCodeRole("RH")      -> SELECT ... WHERE code_role = 'RH'
 *
 * JpaRepository<Role, Long> : "je travaille avec des Role, dont la cle
 * primaire est un Long".
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCodeRole(String codeRole);

    /** La liste des roles, pour le menu "attribuer un role" de l'ADMIN. */
    List<Role> findAllByOrderByCodeRoleAsc();
}
