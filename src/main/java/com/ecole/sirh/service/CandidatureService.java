package com.ecole.sirh.service;

import com.ecole.sirh.model.Candidat;
import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatRepository;
import com.ecole.sirh.repository.CandidatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * La vie d'une candidature :
 *
 *   RECUE  --(entretien planifie)-->  EN_ENTRETIEN  --(evaluation saisie)-->  EVALUEE
 *         --(candidat retenu)-->      RETENUE  --(offre envoyee)-->  OFFRE_ENVOYEE
 *         --(candidat accepte)-->     OFFRE_ACCEPTEE  --(dossier employe)-->  EMBAUCHEE
 *
 * A tout moment on peut aussi passer a REFUSEE.
 *
 * La regle du site : ces changements de statut sont faits par le RH
 * (et la DIRECTION pour la decision d'embauche, voir DecisionService).
 */
@Service
public class CandidatureService {

    public static final String RECUE = "RECUE";
    public static final String EN_ENTRETIEN = "EN_ENTRETIEN";
    public static final String EVALUEE = "EVALUEE";
    public static final String RETENUE = "RETENUE";
    public static final String REFUSEE = "REFUSEE";
    public static final String OFFRE_ENVOYEE = "OFFRE_ENVOYEE";
    public static final String OFFRE_ACCEPTEE = "OFFRE_ACCEPTEE";
    public static final String EMBAUCHEE = "EMBAUCHEE";

    private final CandidatRepository candidatRepository;
    private final CandidatureRepository candidatureRepository;
    private final HistoriqueService historiqueService;

    public CandidatureService(CandidatRepository candidatRepository,
                              CandidatureRepository candidatureRepository,
                              HistoriqueService historiqueService) {
        this.candidatRepository = candidatRepository;
        this.candidatureRepository = candidatureRepository;
        this.historiqueService = historiqueService;
    }

    // ------------------------------------------------------------------
    // 1. DEPOT D UNE CANDIDATURE (depuis le site public ou par le RH)
    // ------------------------------------------------------------------

    @Transactional
    public Candidature deposer(OffreEmploi offre, String prenom, String nom, String email,
                               String telephone, String lettreMotivation, Utilisateur auteur) {

        if (offre == null) {
            throw new RegleMetierException("L'offre est introuvable.");
        }
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            throw new RegleMetierException("Le nom et le prenom du candidat sont obligatoires.");
        }
        if (email == null || email.isBlank()) {
            throw new RegleMetierException("L'email du candidat est obligatoire.");
        }
        if (!"PUBLIEE".equals(offre.getStatutOffre())) {
            throw new RegleMetierException("Cette offre n'est plus ouverte aux candidatures.");
        }

        // Un candidat deja connu (meme email) est reutilise.
        Candidat candidat = candidatRepository.findByEmailCandidat(email.trim()).orElse(null);
        if (candidat == null) {
            candidat = new Candidat();
            candidat.setNomCandidat(nom.trim());
            candidat.setPrenomCandidat(prenom.trim());
            candidat.setEmailCandidat(email.trim());
            candidat.setTelephoneCandidat(telephone);
            candidatRepository.save(candidat);
        }

        // Une seule candidature par candidat et par offre (contrainte UNIQUE).
        if (candidatureRepository.findByCandidat_IdCandidatAndOffre_IdOffre(
                candidat.getIdCandidat(), offre.getIdOffre()).isPresent()) {
            throw new RegleMetierException("Ce candidat a deja postule a cette offre.");
        }

        Candidature candidature = new Candidature();
        candidature.setCandidat(candidat);
        candidature.setOffre(offre);
        candidature.setLettreMotivation(lettreMotivation);
        candidature.setStatutCandidature(RECUE);
        candidatureRepository.save(candidature);

        historiqueService.enregistrer(auteur, "CREATION_CANDIDATURE", "candidature",
                candidature.getIdCandidature().intValue(),
                "Candidature de " + candidat.getPrenomCandidat() + " " + candidat.getNomCandidat()
                        + " sur l'offre " + offre.getTitreOffre() + ".");

        return candidature;
    }

    // ------------------------------------------------------------------
    // 2. CHANGEMENT DE STATUT
    // ------------------------------------------------------------------

    /**
     * Change le statut d'une candidature en kontrolant qu'on ne fait pas
     * un retour en arriere dans le circuit.
     */
    @Transactional
    public Candidature changerStatut(Candidature candidature, String nouveauStatut,
                                     Utilisateur auteur, String commentaire) {

        String ancienStatut = candidature.getStatutCandidature();

        if (ancienStatut.equals(nouveauStatut)) {
            throw new RegleMetierException("La candidature est deja dans le statut "
                    + nouveauStatut + ".");
        }
        if (EMBAUCHEE.equals(ancienStatut)) {
            throw new RegleMetierException("Une candidature embauchee ne peut plus changer de statut.");
        }
        if (!estTransitionAutorisee(ancienStatut, nouveauStatut)) {
            throw new RegleMetierException("Changement de statut impossible : "
                    + ancienStatut + " -> " + nouveauStatut + ".");
        }

        candidature.setStatutCandidature(nouveauStatut);
        candidatureRepository.save(candidature);

        String details = "Statut de la candidature : " + ancienStatut + " -> " + nouveauStatut + ".";
        if (commentaire != null && !commentaire.isBlank()) {
            details = details + " " + commentaire;
        }
        historiqueService.enregistrer(auteur, "CHANGEMENT_STATUT_CANDIDATURE", "candidature",
                candidature.getIdCandidature().intValue(), details);

        return candidature;
    }

    /**
     * Les transitions autorisees du circuit de recrutement.
     * On ne peut pas, par exemple, embaucher quelqu'un qui n'a pas ete retenu.
     */
    private boolean estTransitionAutorisee(String ancien, String nouveau) {
        switch (ancien) {
            case RECUE:
                return EN_ENTRETIEN.equals(nouveau) || REFUSEE.equals(nouveau);
            case EN_ENTRETIEN:
                return EVALUEE.equals(nouveau) || REFUSEE.equals(nouveau);
            case EVALUEE:
                return RETENUE.equals(nouveau) || REFUSEE.equals(nouveau);
            case RETENUE:
                return OFFRE_ENVOYEE.equals(nouveau) || REFUSEE.equals(nouveau);
            case OFFRE_ENVOYEE:
                return OFFRE_ACCEPTEE.equals(nouveau) || REFUSEE.equals(nouveau);
            case OFFRE_ACCEPTEE:
                return EMBAUCHEE.equals(nouveau);
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------
    // 3. LECTURE
    // ------------------------------------------------------------------

    public List<Candidature> listerToutes() {
        return candidatureRepository.findAllByOrderByDateCandidatureDesc();
    }

    public List<Candidature> listerParOffre(Long idOffre) {
        return candidatureRepository.findByOffre_IdOffreOrderByDateCandidatureDesc(idOffre);
    }
}
