package com.ecole.sirh.service;

import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.OffreEmploiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * La vie d'une offre d'emploi.
 *
 *   BROUILLON  --(le RH publie)-->  PUBLIEE  --(le RH cloture)-->  CLOTUREE
 *
 * Une offre est toujours issue d'une demande acceptee : c'est le trace
 * demande -> offre que le cahier des charges demande de conserver.
 */
@Service
public class OffreService {

    private final OffreEmploiRepository offreEmploiRepository;
    private final HistoriqueService historiqueService;

    public OffreService(OffreEmploiRepository offreEmploiRepository,
                        HistoriqueService historiqueService) {
        this.offreEmploiRepository = offreEmploiRepository;
        this.historiqueService = historiqueService;
    }

    /**
     * Cree l'offre correspondant a une demande acceptee.
     * L'offre est d'abord un BROUILLON : le RH peut la completer avant de publier.
     */
    @Transactional
    public OffreEmploi creer(DemandeRecrutement demande, String titre, String description,
                             String lieu, String typeContrat, LocalDate dateCloture,
                             Utilisateur auteur) {

        if (demande == null) {
            throw new RegleMetierException("La demande est introuvable.");
        }
        if (!DemandeService.ACCEPTEE.equals(demande.getStatutDemande())) {
            throw new RegleMetierException("Seule une demande acceptee peut donner lieu a une offre.");
        }
        if (offreEmploiRepository.findByDemande_IdDemande(demande.getIdDemande()).isPresent()) {
            throw new RegleMetierException("Une offre existe deja pour la demande "
                    + demande.getReferenceDemande() + ".");
        }
        if (titre == null || titre.isBlank() || description == null || description.isBlank()) {
            throw new RegleMetierException("Le titre et la description de l'offre sont obligatoires.");
        }

        OffreEmploi offre = new OffreEmploi();
        offre.setDemande(demande);
        offre.setTitreOffre(titre.trim());
        offre.setDescriptionOffre(description.trim());
        offre.setLieuOffre(lieu);
        offre.setTypeContrat(typeContrat == null ? "CDI" : typeContrat);
        offre.setDateCloture(dateCloture);
        offre.setStatutOffre("BROUILLON");
        offreEmploiRepository.save(offre);

        historiqueService.enregistrer(auteur, "CREATION_OFFRE", "offre_emploi",
                offre.getIdOffre().intValue(),
                "Offre " + offre.getTitreOffre() + " creee (brouillon) a partir de la demande "
                        + demande.getReferenceDemande() + ".");

        return offre;
    }

    /** Le RH publie l'offre : elle devient visible sur le site public. */
    @Transactional
    public OffreEmploi publier(OffreEmploi offre, LocalDate dateCloture, Utilisateur auteur) {

        if (offre == null) {
            throw new RegleMetierException("L'offre est introuvable.");
        }
        if (!"BROUILLON".equals(offre.getStatutOffre())) {
            throw new RegleMetierException("Seule une offre en brouillon peut etre publiee.");
        }
        if (offre.getTitreOffre() == null || offre.getTitreOffre().isBlank()) {
            throw new RegleMetierException("Le titre de l'offre est obligatoire.");
        }

        offre.setDatePublication(LocalDateTime.now());
        offre.setDateCloture(dateCloture);
        offre.setStatutOffre("PUBLIEE");
        offreEmploiRepository.save(offre);

        historiqueService.enregistrer(auteur, "PUBLICATION_OFFRE", "offre_emploi",
                offre.getIdOffre().intValue(),
                "Offre " + offre.getTitreOffre() + " publiee.");
        return offre;
    }

    /** Le RH cloture l'offre : plus aucune candidature possible. */
    @Transactional
    public OffreEmploi cloturer(OffreEmploi offre, Utilisateur auteur) {
        if (offre == null) {
            throw new RegleMetierException("L'offre est introuvable.");
        }
        if (!"PUBLIEE".equals(offre.getStatutOffre())) {
            throw new RegleMetierException("Seule une offre publiee peut etre cloturee.");
        }
        offre.setStatutOffre("CLOTUREE");
        offreEmploiRepository.save(offre);

        historiqueService.enregistrer(auteur, "CLOTURE_OFFRE", "offre_emploi",
                offre.getIdOffre().intValue(),
                "Offre " + offre.getTitreOffre() + " cloturee.");
        return offre;
    }

    // ------------------------------------------------------------------
    // LECTURE
    // ------------------------------------------------------------------

    public List<OffreEmploi> listerToutes() {
        return offreEmploiRepository.findAllByOrderByIdOffreDesc();
    }

    /** Les offres visibles par le public (site carrieres). */
    public List<OffreEmploi> listerPubliees() {
        return offreEmploiRepository.findAllByStatutOffreInOrderByDatePublicationDesc(
                List.of("PUBLIEE"));
    }
}
