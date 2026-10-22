package com.ecole.sirh.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de securite : on verifie que les regles de roles sont bien appliquees.
 *
 * Ces tests utilisent la VRAIE base de donnees (comme DepartementRepositoryTest),
 * car la connexion lit les comptes dans PostgreSQL.
 *
 * @Transactional : tout ce qui est ecrit pendant le test est annule a la fin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- 1. Une page protegee renvoie vers la page de connexion ----------

    @Test
    void unePageProtegeeRedirigeVersLaConnexion() throws Exception {
        // "accept text/html" = on simule un navigateur (comme le menu du site).
        mockMvc.perform(get("/dashboard").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertTrue(result.getResponse().getRedirectedUrl().contains("/login")));
    }

    @Test
    void unAppelSansAuthentificationRenvoie401() throws Exception {
        // Un appel de type API (pas un navigateur) doit renvoyer 401.
        mockMvc.perform(get("/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ---------- 2. La connexion ----------

    @Test
    void connexionReussieAvecUnCompteActif() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "rh@ecole.fr")
                        .param("password", "midera2026"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertTrue(result.getResponse().getRedirectedUrl().contains("/dashboard"));
    }

    @Test
    void connexionRefuseeAvecUnMauvaisMotDePasse() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "rh@ecole.fr")
                        .param("password", "mauvais-mot-de-passe"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertEquals("/login?error", result.getResponse().getRedirectedUrl());
    }

    @Test
    void unCompteDesactiveNePeutPasSeConnecter() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "manager.archive@ecole.fr")
                        .param("password", "midera2026"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertEquals("/login?error", result.getResponse().getRedirectedUrl());
    }

    // ---------- 3. Les droits de chaque role ----------

    @Test
    void leRhVoitLeTableauDeBord() throws Exception {
        mockMvc.perform(get("/dashboard").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tableau de bord")));
    }

    @Test
    void lAdminVoitLaPageDesUtilisateurs() throws Exception {
        mockMvc.perform(get("/admin/utilisateurs").with(httpBasic("admin@ecole.fr", "midera2026")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Comptes utilisateurs et roles")));
    }

    @Test
    void leManagerNAccedePasAuxUtilisateurs() throws Exception {
        mockMvc.perform(get("/admin/utilisateurs").with(httpBasic("manager@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    void leRhNAccedePasAuxUtilisateurs() throws Exception {
        mockMvc.perform(get("/admin/utilisateurs").with(httpBasic("rh@ecole.fr", "midera2026")))
                .andExpect(status().isForbidden());
    }

    @Test
    void leManagerNePeutPasCreerUnDepartement() throws Exception {
        mockMvc.perform(post("/departements/nouveau")
                        .with(csrf())
                        .with(httpBasic("manager@ecole.fr", "midera2026"))
                        .param("nomDepartement", "Departement interdit"))
                .andExpect(status().isForbidden());
    }

    @Test
    void leRhPeutCreerUnDepartement() throws Exception {
        MvcResult result = mockMvc.perform(post("/departements/nouveau")
                        .with(csrf())
                        .with(httpBasic("rh@ecole.fr", "midera2026"))
                        .param("nomDepartement", "Departement de test")
                        .param("budgetAnnuel", "1000"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Le RH est redirige vers la liste : on ne doit PAS etre renvoye
        // vers la page de connexion (ce qui signalerait un echec).
        assertTrue(result.getResponse().getRedirectedUrl().contains("/departements"));
        assertFalse(result.getResponse().getRedirectedUrl().contains("/login"));
    }
}
