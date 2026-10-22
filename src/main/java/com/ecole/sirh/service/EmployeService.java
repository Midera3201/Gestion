package com.ecole.sirh.service;

import com.ecole.sirh.model.Candidature;
import com.ecole.sirh.model.Contrat;
import com.ecole.sirh.model.DemandeRecrutement;
import com.ecole.sirh.model.Employe;
import com.ecole.sirh.model.OffreEmploi;
import com.ecole.sirh.model.TacheIntegration;
import com.ecole.sirh.model.Utilisateur;
import com.ecole.sirh.repository.CandidatureRepository;
import com.ecole.sirh.repository.ContratRepository;
import com.ecole.sirh.repository.EmployeRepository;
import com.ecole.sirh.repository.TacheIntegrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * La creation du dossier employe, du contrat et des taches d integration.
 *
 * Un employe n'est PAS cree directement par un formulaire : il nait d'une
 * candidature acceptee. C'est le trace complet qui est demande :
 *   demande -> offre -> candidature -> decision -> employe -> contrat.
 */
@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final TacheIntegrationRepository tacheIntegrationRepository;
    private final CandidatureRepository candidatureRepository;
    private final HistoriqueService historiqueService;

    public EmployeService(EmployeRepository employeRepository,
                          ContratRepository contratRepository,
                          TacheIntegrationRepository tacheIntegrationRepository,
                          CandidatureRepository candidatureRepository,
                          HistoriqueService historiqueService) {
        this.employeRepository = employeRepository;
        this.contratRepository = contratRepository;
        this.tacheIntegrationRepository = tacheIntegrationRepository;
        this.candidatureRepository = candidatureRepository;
        this.historiqueService = historiqueService;
    }

    // ------------------------------------------------------------------
    // 1. CREATION DU DOSSIER EMPLOYE
    // ------------------------------------------------------------------

    @Transactional
    public Employe creerDepuisCandidature(Candidature candidature, LocalDate dateEmbauche,
                                          Utilisateur auteur) {

        if (candidature == null) {
            throw new RegleMetierException("La candidature est introuvable.");
        }
        if (!"OFFRE_ACCEPTEE".equals(candidature.getStatutCandidature())) {
            throw new RegleMetierException("On ne cree un dossier employe qu'apres "
                    + "l'acceptation de l'offre par le candidat.");
        }
        if (employeRepository.findByCandidature_IdCandidature(
                candidature.getIdCandidature()).isPresent()) {
            throw new RegleMetierException("Un dossier employe existe deja pour cette candidature.");
        }
        if (dateEmbauche == null) {
            throw new RegleMetierException("La date d'embauche est obligatoire.");
        }

        OffreEmploi offre = candidature.getOffre();
        DemandeRecrutement demande = offre.getDemande();

        Employe employe = new Employe();
        employe.setMatricule(prochainMatricule());
        employe.setNomEmploye(candidature.getCandidat().getNomCandidat());
        employe.setPrenomEmploye(candidature.getCandidat().getPrenomCandidat());
        employe.setEmailEmploye(candidature.getCandidat().getEmailCandidat());
        employe.setDateEmbauche(dateEmbauche);
        employe.setStatutEmploye("ACTIF");
        employe.setPoste(demande.getPoste());
        employe.setDepartement(demande.getDepartement());
        employe.setCandidature(candidature);
        employeRepository.save(employe);

        // La candidature passe a EMBAUCHEE : c'est le dernier maillon du circuit.
        candidature.setStatutCandidature("EMBAUCHEE");
        candidatureRepository.save(candidature);

        // Le plan d'integration standard du nouvel employe.
        creerTachesIntegration(employe, auteur);

        historiqueService.enregistrer(auteur, "CREATION_EMPLOYE", "employe",
                employe.getIdEmploye().intValue(),
                "Dossier " + employe.getMatricule() + " cree pour "
                        + employe.getPrenomEmploye() + " " + employe.getNomEmploye()
                        + " (" + demande.getPoste().getTitrePoste() + ").");

        return employe;
    }

    /** Matricule EMP-2026-001, EMP-2026-002... */
    private String prochainMatricule() {
        String annee = String.valueOf(LocalDate.now().getYear());
        int numero = (int) employeRepository.count() + 1;
        String matricule;
        do {
            matricule = "EMP-" + annee + "-" + String.format("%03d", numero);
            numero++;
        } while (employeRepository.findByMatricule(matricule).isPresent());
        return matricule;
    }

    /** Les 5 etapes d integration creees automatiquement. */
    private void creerTachesIntegration(Employe employe, Utilisateur auteur) {
        String[] libelles = {
                "Signature du contrat de travail",
                "Remise du materiel informatique",
                "Creation du compte utilisateur",
                "Formation securite et RGPD",
                "Definition des objectifs a 90 jours"
        };
        for (String libelle : libelles) {
            TacheIntegration tache = new TacheIntegration();
            tache.setLibelleTache(libelle);
            tache.setStatutTache("A_FAIRE");
            tache.setDatePrevue(employe.getDateEmbauche());
            tache.setEmploye(employe);
            tache.setResponsable(auteur);
            tacheIntegrationRepository.save(tache);
        }
    }

    // ------------------------------------------------------------------
    // 2. CONTRAT
    // ------------------------------------------------------------------

    @Transactional
    public Contrat creerContrat(Employe employe, String typeContrat, LocalDate dateDebut,
                                 LocalDate dateFin, java.math.BigDecimal salaire, Utilisateur auteur) {

        if (employe == null) {
            throw new RegleMetierException("L'employe est introuvable.");
        }
        if (typeContrat == null || typeContrat.isBlank()) {
            throw new RegleMetierException("Le type de contrat est obligatoire.");
        }
        if (dateDebut == null) {
            throw new RegleMetierException("La date de debut est obligatoire.");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new RegleMetierException("La date de fin doit etre posterieure a la date de debut.");
        }
        if (salaire == null || salaire.signum() <= 0) {
            throw new RegleMetierException("Le salaire doit etre strictement positif.");
        }

        Contrat contrat = new Contrat();
        contrat.setEmploye(employe);
        contrat.setTypeContrat(typeContrat);
        contrat.setDateDebut(dateDebut);
        contrat.setDateFin(dateFin);
        contrat.setSalaire(salaire);
        contrat.setStatutContrat("BROUILLON");
        contratRepository.save(contrat);

        historiqueService.enregistrer(auteur, "CREATION_CONTRAT", "contrat",
                contrat.getIdContrat().intValue(),
                "Contrat " + typeContrat + " cree pour " + employe.getMatricule() + ".");

        return contrat;
    }

    /** Le RH signe le contrat. */
    @Transactional
    public Contrat signerContrat(Contrat contrat, LocalDate dateSignature, Utilisateur auteur) {
        if (contrat == null) {
            throw new RegleMetierException("Le contrat est introuvable.");
        }
        if ("SIGNE".equals(contrat.getStatutContrat())) {
            throw new RegleMetierException("Ce contrat est deja signe.");
        }
        contrat.setDateSignature(dateSignature);
        contrat.setStatutContrat("SIGNE");
        contratRepository.save(contrat);

        historiqueService.enregistrer(auteur, "SIGNATURE_CONTRAT", "contrat",
                contrat.getIdContrat().intValue(),
                "Contrat signe le " + dateSignature + ".");
        return contrat;
    }

    // ------------------------------------------------------------------
    // 3. TACHES D INTEGRATION
    // ------------------------------------------------------------------

    @Transactional
    public TacheIntegration changerStatutTache(TacheIntegration tache, String statut,
                                               LocalDate dateRealisation, Utilisateur auteur) {
        if (tache == null) {
            throw new RegleMetierException("La tache est introuvable.");
        }
        if (!"A_FAIRE".equals(statut) && !"EN_COURS".equals(statut) && !"TERMINEE".equals(statut)) {
            throw new RegleMetierException("Statut de tache invalide : " + statut);
        }
        tache.setStatutTache(statut);
        tache.setDateRealisation(dateRealisation);
        tacheIntegrationRepository.save(tache);

        historiqueService.enregistrer(auteur, "MAJ_TACHE_INTEGRATION", "tache_integration",
                tache.getIdTache().intValue(),
                "Tache '" + tache.getLibelleTache() + " -> " + statut + ".");
        return tache;
    }

    // ------------------------------------------------------------------
    // 4. LECTURE
    // ------------------------------------------------------------------

    public List<Employe> listerTous() {
        return employeRepository.findAllByOrderByNomEmployeAsc();
    }

    public List<Contrat> listerContrats() {
        return contratRepository.findAllByOrderByDateDebutDesc();
    }
}
