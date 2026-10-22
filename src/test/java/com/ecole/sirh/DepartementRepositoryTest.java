package com.ecole.sirh;

import com.ecole.sirh.model.Departement;
import com.ecole.sirh.repository.DemandeRecrutementRepository;
import com.ecole.sirh.repository.DepartementRepository;
import com.ecole.sirh.repository.PosteRepository;
import com.ecole.sirh.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'integration : on verifie que JPA ecrit vraiment dans PostgreSQL.
 *
 * @SpringBootTest : demarre toute l'application (comme au demarrage reel),
 *                    avec la vraie configuration de application.properties,
 *                    donc la VRAIE base "rh".
 * @Transactional   : chaque test est joue dans une transaction qui est
 *                    ANNULEE a la fin. Les donnees de test ne restent donc
 *                    pas dans la base.
 */
@SpringBootTest
@Transactional
class DepartementRepositoryTest {

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private PosteRepository posteRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DemandeRecrutementRepository demandeRepository;

    @Test
    void je_peux_enregistrer_puis_relire_un_departement() {
        // On compte ce qu'il y a deja
        long avant = departementRepository.count();

        Departement nouveau = new Departement("Departement de test",
                "Cree par le test unitaire", new BigDecimal("12345.00"));

        // save() = INSERT en base (Hibernate genere le INSERT)
        Departement enregistre = departementRepository.save(nouveau);

        // Hibernate a rempli la cle primaire generee par PostgreSQL (SERIAL)
        assertNotNull(enregistre.getIdDepartement());
        assertTrue(enregistre.getIdDepartement() > 0);

        // On relit la ligne depuis la base
        Optional<Departement> relu = departementRepository.findById(enregistre.getIdDepartement());
        assertTrue(relu.isPresent());
        assertEquals("Departement de test", relu.get().getNomDepartement());
        assertEquals(new BigDecimal("12345.00"), relu.get().getBudgetAnnuel());

        // Le compteur a bien augmente
        assertEquals(avant + 1, departementRepository.count());
    }

    @Test
    void les_donnees_de_l_etape_2_sont_toujours_en_base() {
        assertTrue(departementRepository.count() >= 5, "Il devrait y avoir au moins 5 departements");
        assertTrue(posteRepository.count() >= 6, "Il devrait y avoir au moins 6 postes");
        assertTrue(utilisateurRepository.count() >= 7, "Il devrait y avoir au moins 7 utilisateurs");
        assertTrue(demandeRepository.count() >= 5, "Il devrait y avoir au moins 5 demandes");
    }

    @Test
    void une_demande_est_liee_a_son_poste_et_a_son_demandeur() {
        // On prend la premiere demande de la base
        var demande = demandeRepository.findAllByOrderByIdDemandeDesc().get(0);

        // Les relations JPA doivent etre remplies
        assertNotNull(demande.getPoste());
        assertNotNull(demande.getDepartement());
        assertNotNull(demande.getDemandeur());
        assertNotNull(demande.getPoste().getDepartement());
    }
}
