package com.ecole.sirh.service;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.DecisionEmbauche;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.DecisionEmbaucheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * La decision finale d'embauche : elle appartient a la DIRECTION.
 *
 * Elle porte sur une candidature qui a ete RETENUE (apres les entretiens
 * et les evaluations).
 *
 *   APPROUVEE -> la candidature passe a OFFRE_ENVOYEE (le RH envoie l'offre
 *                au candidat, puis le dossier employe est cree)
 *   REFUSEE   -> la candidature passe a REFUSEE
 *
 * Rappel de la separation des pouvoirs : ni le MANAGER, ni le RH, ni l'ADMIN
 * ne peuvent prendre cette decision.
 */
@Service
public class DecisionService {

    private final DecisionEmbaucheRepository decisionEmbaucheRepository;
    private final CandidatureService candidatureService;
    private final HistoriqueService historiqueService;

    public DecisionService(DecisionEmbaucheRepository decisionEmbaucheRepository,
                           CandidatureService candidatureService,
                           HistoriqueService historiqueService) {
        this.decisionEmbaucheRepository = decisionEmbaucheRepository;
        this.candidatureService = candidatureService;
        this.historiqueService = historiqueService;
    }

    @Transactional
    public DecisionEmbauche decider(Candidature candidature, boolean approuvee,
                                    String commentaire, Utilisateur decideur) {

        if (candidature == null) {
            throw new RegleMetierException("La candidature est introuvable.");
        }
        if (!"RETENUE".equals(candidature.getStatutCandidature())) {
            throw new RegleMetierException("La decision d'embauche ne porte que sur une "
                    + "candidature retenue. Statut actuel : " + candidature.getStatutCandidature() + ".");
        }
        if (decisionEmbaucheRepository.findByCandidature_IdCandidature(
                candidature.getIdCandidature()).isPresent()) {
            throw new RegleMetierException("Une decision a deja ete prise pour cette candidature.");
        }

        DecisionEmbauche decision = new DecisionEmbauche();
        decision.setCandidature(candidature);
        decision.setDecideur(decideur);
        decision.setDecisionFinale(approuvee ? "APPROUVEE" : "REFUSEE");
        decision.setCommentaireDecision(commentaire);
        decisionEmbaucheRepository.save(decision);

        // On fait passer la candidature a l'etat suivant du circuit.
        candidatureService.changerStatut(candidature,
                approuvee ? "OFFRE_ENVOYEE" : "REFUSEE",
                decideur,
                approuvee ? "Decision d'embauche approuvee par la Direction."
                          : "Decision d'embauche refusee par la Direction.");

        historiqueService.enregistrer(decideur, "DECISION_EMBAUCHE", "candidature",
                candidature.getIdCandidature().intValue(),
                (approuvee ? "Embauche approuvee" : "Embauche refusee") + " pour "
                        + candidature.getCandidat().getPrenomCandidat() + " "
                        + candidature.getCandidat().getNomCandidat() + ".");

        return decision;
    }
}
