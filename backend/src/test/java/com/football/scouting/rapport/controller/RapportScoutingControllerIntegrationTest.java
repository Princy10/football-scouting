package com.football.scouting.rapport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class RapportScoutingControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("football_scouting_rapport_test")
            .withUsername("scouting_user")
            .withPassword("scouting_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RapportScoutingRepository rapportScoutingRepository;

    @Autowired
    private JoueurRepository joueurRepository;

    @BeforeEach
    void setUp() {
        rapportScoutingRepository.deleteAll();
        joueurRepository.deleteAll();
    }

    @Test
    void createRapport_shouldReturnCreatedRapport() throws Exception {
        Joueur joueur = saveJoueur();

        mockMvc.perform(post("/api/rapports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(joueur.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.joueurId").value(joueur.getId()))
                .andExpect(jsonPath("$.dateObservation").value("2026-07-15"))
                .andExpect(jsonPath("$.matchObserve").value("Arsenal FC - Chelsea FC"))
                .andExpect(jsonPath("$.scoreGlobal").isEmpty());
    }

    @Test
    void getAllRapports_shouldReturnPaginatedRapports()
            throws Exception {

        Joueur joueur = saveJoueur();

        RapportScouting ancien =
                rapport(joueur);

        ancien.setDateObservation(
                LocalDate.of(2026, 5, 1)
        );

        ancien.setScoutName("Ancien Scout");

        RapportScouting recent =
                rapport(joueur);

        recent.setDateObservation(
                LocalDate.of(2026, 5, 20)
        );

        recent.setScoutName("Jean Scout");

        rapportScoutingRepository.save(ancien);
        rapportScoutingRepository.save(recent);

        mockMvc.perform(
                        get("/api/rapports")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].scoutName"
                        )
                                .value("Jean Scout")
                )
                .andExpect(
                        jsonPath("$.page")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalPages")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.first")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.last")
                                .value(false)
                );
    }

    @Test
    void getAllRapports_shouldCombineAllFilters()
            throws Exception {

        Joueur cible =
                joueurRepository.save(
                        Joueur.builder()
                                .nom("Mbappé")
                                .prenom("Kylian")
                                .postePrincipal("Attaquant")
                                .build()
                );

        Joueur autreJoueur =
                joueurRepository.save(
                        Joueur.builder()
                                .nom("Wirtz")
                                .prenom("Florian")
                                .postePrincipal("Milieu")
                                .build()
                );

        RapportScouting rapportCible =
                rapport(cible);

        rapportCible.setDateObservation(
                LocalDate.of(2026, 5, 15)
        );

        rapportCible.setMatchObserve(
                "Analyse finale européenne"
        );

        rapportCible.setScoreGlobal(88);
        rapportCible.setRecommandation(
                "RECOMMANDE"
        );
        rapportCible.setScoutName(
                "Alice Dupont"
        );

        RapportScouting mauvaisJoueur =
                rapport(autreJoueur);

        mauvaisJoueur.setDateObservation(
                LocalDate.of(2026, 5, 15)
        );

        mauvaisJoueur.setMatchObserve(
                "Analyse finale européenne"
        );

        mauvaisJoueur.setScoreGlobal(88);
        mauvaisJoueur.setRecommandation(
                "RECOMMANDE"
        );
        mauvaisJoueur.setScoutName(
                "Alice Dupont"
        );

        RapportScouting mauvaisScore =
                rapport(cible);

        mauvaisScore.setDateObservation(
                LocalDate.of(2026, 5, 15)
        );

        mauvaisScore.setMatchObserve(
                "Analyse finale européenne"
        );

        mauvaisScore.setScoreGlobal(65);
        mauvaisScore.setRecommandation(
                "A_DEVELOPPER"
        );
        mauvaisScore.setScoutName(
                "Alice Dupont"
        );

        rapportScoutingRepository.save(
                rapportCible
        );

        rapportScoutingRepository.save(
                mauvaisJoueur
        );

        rapportScoutingRepository.save(
                mauvaisScore
        );

        mockMvc.perform(
                        get("/api/rapports")
                                .param(
                                        "search",
                                        "finale"
                                )
                                .param(
                                        "joueurId",
                                        cible.getId().toString()
                                )
                                .param(
                                        "scoreMin",
                                        "80"
                                )
                                .param(
                                        "scoreMax",
                                        "90"
                                )
                                .param(
                                        "recommandation",
                                        "recommande"
                                )
                                .param(
                                        "dateObservationMin",
                                        "2026-05-01"
                                )
                                .param(
                                        "dateObservationMax",
                                        "2026-05-31"
                                )
                                .param(
                                        "scout",
                                        "alice"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].joueurId"
                        )
                                .value(cible.getId())
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].scoreGlobal"
                        )
                                .value(88)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].recommandation"
                        )
                                .value("RECOMMANDE")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].scoutName"
                        )
                                .value("Alice Dupont")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllRapports_shouldReturn400_whenScoreRangeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/rapports")
                                .param("scoreMin", "90")
                                .param("scoreMax", "70")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Le score minimal ne doit pas "
                                                + "être supérieur au "
                                                + "score maximal."
                                )
                );
    }

    @Test
    void getAllRapports_shouldReturn400_whenScoreIsAbove100()
            throws Exception {

        mockMvc.perform(
                        get("/api/rapports")
                                .param("scoreMin", "110")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Le score minimal doit être "
                                                + "compris entre 0 et 100."
                                )
                );
    }

    @Test
    void getAllRapports_shouldReturn400_whenDateRangeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/rapports")
                                .param(
                                        "dateObservationMin",
                                        "2026-06-01"
                                )
                                .param(
                                        "dateObservationMax",
                                        "2026-05-01"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La date d'observation minimale "
                                                + "ne doit pas être "
                                                + "postérieure à la date "
                                                + "maximale."
                                )
                );
    }

    @Test
    void getRapportById_shouldReturnRapport_whenExists() throws Exception {
        RapportScouting saved = rapportScoutingRepository.save(rapport(saveJoueur()));

        mockMvc.perform(get("/api/rapports/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.joueurId").value(saved.getJoueur().getId()));
    }

    @Test
    void updateRapport_shouldReturnUpdatedRapport() throws Exception {
        RapportScouting saved = rapportScoutingRepository.save(rapport(saveJoueur()));
        RapportScoutingRequest request = request(saved.getJoueur().getId());
        request.setRecommandation("À recruter");

        mockMvc.perform(put("/api/rapports/{id}", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommandation").value("À recruter"))
                .andExpect(jsonPath("$.scoreGlobal").isEmpty());
    }

    @Test
    void deleteRapport_shouldReturn204_whenRapportExists() throws Exception {
        RapportScouting saved = rapportScoutingRepository.save(rapport(saveJoueur()));

        mockMvc.perform(delete("/api/rapports/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRapportById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/rapports/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Rapport de scouting introuvable avec l'id : 99999"));
    }

    @Test
    void createRapport_shouldReturn404_whenJoueurDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/rapports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(99999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Joueur introuvable avec l'id : 99999"));
    }

    @Test
    void createRapport_shouldReturn400_whenValidationFails() throws Exception {
        RapportScoutingRequest request = request(null);
        request.setDateObservation(null);

        mockMvc.perform(post("/api/rapports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", hasKey("joueurId")))
                .andExpect(jsonPath("$.validationErrors", hasKey("dateObservation")));
    }

    private RapportScoutingRequest request(Long joueurId) {
        return RapportScoutingRequest.builder()
                .joueurId(joueurId)
                .dateObservation(LocalDate.of(2026, 7, 15))
                .matchObserve("Arsenal FC - Chelsea FC")
                .commentaireGeneral("Bonne vision du jeu et excellente qualité de passe.")
                .recommandation("À suivre")
                .scoutName("Jean Scout")
                .build();
    }

    private RapportScouting rapport(Joueur joueur) {
        return RapportScouting.builder()
                .joueur(joueur)
                .dateObservation(LocalDate.of(2026, 7, 15))
                .matchObserve("Arsenal FC - Chelsea FC")
                .commentaireGeneral("Bonne vision du jeu et excellente qualité de passe.")
                .recommandation("À suivre")
                .scoutName("Jean Scout")
                .build();
    }

    private Joueur saveJoueur() {
        return joueurRepository.save(Joueur.builder()
                .nom("Mbappé")
                .prenom("Kylian")
                .postePrincipal("Milieu")
                .build());
    }
}
