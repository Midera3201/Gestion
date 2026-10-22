package com.ecole.sirh.securite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ETAPE 11 : les tests de securite des NOUVEAUX modules.
 *
 * On verifie, page par page, que chaque role n'accede qu'a ce qui le
 * concerne. C'est la "matrice des droits" du cahier des charges,
 * traduite en tests automatises.
 *
 * @Transactional : les creations faites par les tests sont annulees.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecuriteRecrutementTest {

    @Autowired
    private MockMvc mockMvc;

    // ------------------------------------------------------------------
    // 1. LE SITE PUBLIC (sans connexion)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Le site public des carrieres est ouvert a tout le monde")
    void leSitePublicEstOuvert() throws Exception {
        mockMvc.perform(get("/carrieres").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("La page publique d'une offre publiee est ouverte")
    void laPagePubliqueDOffreEstOuverte() throws Exception {
        // L'offre 2 est PUBLIEE dans 02_data.sql.
        mockMvc.perform(get("/carrieres/2").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Une offre cloturee n'apparait plus dans le site public")
    void uneOffreClotureeNestPasVisible() throws Exception {
        // L'offre 1 est CLOTUREE : le visiteur est renvoye vers la liste.
        mockMvc.perform(get("/carrieres/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrieres"));
    }

    @Test
    @DisplayName("Un visiteur peut postuler sans etre connecte")
    void unVisiteurPeutPostuler() throws Exception {
        MvcResult result = mockMvc.perform(post("/carrieres/2/postuler")
                        .with(csrf())
                        .param("prenom", "Test")
                        .param("nom", "Visiteur")
                        .param("email", "test.visiteur@mail.com")
                        .param("telephone", "0600000000")
                        .param("lettreMotivation", "Je postule a ce poste."))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // On est renvoye sur la page de l'offre, jamais vers /login.
        assertFalse(result.getResponse().getRedirectedUrl().contains("/login"));
        assertTrue(result.getResponse().getRedirectedUrl().contains("/carrieres"));
    }

    // ------------------------------------------------------------------
    // 2. L ESPACE RH : OFFRES, CANDIDATURES, EMPLOYES
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Le RH voit la page des offres")
    void leRhVoitLesOffres() throws Exception {
        mockMvc.perform(get("/offres").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Le MANAGER n'accede pas a la gestion des offres")
    void leManagerNAccedePasAuxOffres() throws Exception {
        mockMvc.perform(get("/offres").with(httpBasic("manager@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Le RH voit les candidatures et les employes")
    void leRhVoitCandidaturesEtEmployes() throws Exception {
        mockMvc.perform(get("/candidatures").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/employes").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Le MANAGER n'accede pas aux dossiers employes")
    void leManagerNAccedePasAuxEmployes() throws Exception {
        mockMvc.perform(get("/employes").with(httpBasic("manager@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Le RH peut faire avancer une candidature")
    void leRhPeutChangerLeStatutDUneCandidature() throws Exception {
        // La candidature 3 est au statut RECUE : le RH peut la passer en entretien.
        MvcResult result = mockMvc.perform(post("/candidatures/3/statut")
                        .with(csrf())
                        .with(httpBasic("rh@ecole.fr", "midera2026"))
                        .param("statut", "EN_ENTRETIEN"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertFalse(result.getResponse().getRedirectedUrl().contains("/login"));
    }

    @Test
    @DisplayName("Le MANAGER ne change pas le statut d'une candidature")
    void leManagerNeChangePasLeStatut() throws Exception {
        mockMvc.perform(post("/candidatures/3/statut")
                        .with(csrf())
                        .with(httpBasic("manager@ecole.fr", "midera2026"))
                        .param("statut", "REFUSEE"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // 3. LES ENTRETIENS : RH ET MANAGER
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Le RH et le MANAGER voient les entretiens")
    void rhEtManagerVoientLesEntretiens() throws Exception {
        mockMvc.perform(get("/entretiens").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/entretiens").with(httpBasic("manager@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Le FINANCE n'accede pas aux entretiens")
    void leFinanceNAccedePasAuxEntretiens() throws Exception {
        mockMvc.perform(get("/entretiens").with(httpBasic("finance@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Le MANAGER peut donner un avis sur un entretien")
    void leManagerPeutEvaluer() throws Exception {
        // L'entretien 3 est planifie (candidature 4).
        MvcResult result = mockMvc.perform(post("/entretiens/3/evaluation")
                        .with(csrf())
                        .with(httpBasic("manager.marketing@ecole.fr", "midera2026"))
                        .param("noteEvaluation", "4")
                        .param("avisEvaluation", "FAVORABLE")
                        .param("pointsForts", "Bonne argumentation")
                        .param("pointsFaibles", "Peu d experience"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertFalse(result.getResponse().getRedirectedUrl().contains("/login"));
    }

    // ------------------------------------------------------------------
    // 4. LA DECISION D EMBAUCHE : DIRECTION SEULE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("La DIRECTION voit la page des decisions")
    void laDirectionVoitLesDecisions() throws Exception {
        mockMvc.perform(get("/decisions").with(httpBasic("direction@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ni le RH, ni le MANAGER, ni l'ADMIN ne voient la page des decisions")
    void laDecisionEstReserveeALaDirection() throws Exception {
        mockMvc.perform(get("/decisions").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/decisions").with(httpBasic("manager@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/decisions").with(httpBasic("admin@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Le RH ne peut pas approuver une embauche en direct")
    void leRhNePeutPasApprouverUneEmbauche() throws Exception {
        mockMvc.perform(post("/decisions/candidatures/3/decision")
                        .with(csrf())
                        .with(httpBasic("rh@ecole.fr", "midera2026"))
                        .param("approuvee", "true"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // 5. LE POSTE D UN MANAGER : CREER UNE DEMANDE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Le MANAGER peut creer une demande de recrutement")
    void leManagerPeutCreerUneDemande() throws Exception {
        MvcResult result = mockMvc.perform(post("/demandes/nouvelle")
                        .with(csrf())
                        .with(httpBasic("manager@ecole.fr", "midera2026"))
                        .param("idPoste", "1")
                        .param("idDepartement", "3")
                        .param("motifDemande", "Demande creee par un test.")
                        .param("nombrePostes", "1")
                        .param("budgetEstime", "42000")
                        .param("dateBesoin", "2027-02-01")
                        .param("financeRequise", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertFalse(result.getResponse().getRedirectedUrl().contains("/login"));
    }

    @Test
    @DisplayName("Le RH n'a pas le droit de creer une demande (ce n'est pas son role)")
    void leRhNePeutPasCreerUneDemande() throws Exception {
        mockMvc.perform(post("/demandes/nouvelle")
                        .with(csrf())
                        .with(httpBasic("rh@ecole.fr", "midera2026"))
                        .param("idPoste", "1")
                        .param("idDepartement", "3")
                        .param("motifDemande", "Tentative interdite.")
                        .param("nombrePostes", "1")
                        .param("budgetEstime", "1000")
                        .param("dateBesoin", "2027-02-01"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // 6. L HISTORIQUE : ADMIN SEUL
    // ------------------------------------------------------------------

    @Test
    @DisplayName("L'ADMIN voit l'historique des actions")
    void lAdminVoitLHistorique() throws Exception {
        mockMvc.perform(get("/historique").with(httpBasic("admin@ecole.fr", "midera2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Les autres roles n'accedent pas a l'historique")
    void lHistoriqueEstReserveALAdmin() throws Exception {
        mockMvc.perform(get("/historique").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/historique").with(httpBasic("direction@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // 7. TOUTES LES NOUVELLES PAGES DEMANDENT UNE CONNEXION
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Les nouvelles pages sont fermees aux visiteurs non connectes")
    void lesNouvellesPagesSontFermees() throws Exception {
        mockMvc.perform(get("/demandes").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/offres").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/candidatures").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/entretiens").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/decisions").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/employes").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/historique").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }
}
