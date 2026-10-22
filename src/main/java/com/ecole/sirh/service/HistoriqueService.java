package com.ecole.sirh.service;

import com.ecole.sirh.model.HistoriqueAction;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.HistoriqueActionRepository;
import org.springframework.stereotype.Service;

/**
 * Le journal des actions ("historique_action").
 *
 * Chaque action importante du site (creation d'une demande, validation,
 * publication d'une offre, creation d'un dossier employe...) est ecrite
 * dans la table historique_action avec :
 *   - qui l'a fait (id_utilisateur)
 *   - quoi (action_effectuee)
 *   - sur quoi (entite_concernee + id_entite)
 *
 * C'est la tracabilite demandee dans le cahier des charges.
 * Seuls les administrateurs consultent ce journal (page /historique).
 */
@Service
public class HistoriqueService {

    private final HistoriqueActionRepository historiqueActionRepository;

    public HistoriqueService(HistoriqueActionRepository historiqueActionRepository) {
        this.historiqueActionRepository = historiqueActionRepository;
    }

    /**
     * Ecrit une ligne dans le journal.
     *
     * @param auteur        l'utilisateur connecte (null = action anonyme, on ignore)
     * @param action        le code de l'action, ex : "VALIDATION_DEMANDE"
     * @param entite        le nom de la table concernee, ex : "demande_recrutement"
     * @param idEntite      l'identifiant de la ligne concernee (peut etre null)
     * @param details       une phrase qui explique l'action
     */
    public void enregistrer(Utilisateur auteur, String action, String entite,
                            Integer idEntite, String details) {

        // Une action anonyme (postulation d'un candidat sur le site public)
        // n'est pas rattachee a un compte : on ne l'ecrit pas dans le journal
        // des utilisateurs connectes.
        if (auteur == null) {
            return;
        }

        HistoriqueAction ligne = new HistoriqueAction();
        ligne.setActionEffectuee(action);
        ligne.setEntiteConcernee(entite);
        ligne.setIdEntite(idEntite);
        ligne.setDetails(details);
        ligne.setUtilisateur(auteur);

        historiqueActionRepository.save(ligne);
    }
}
