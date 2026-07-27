package com.football.scouting.note.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.note.dto.NoteCritereRequest;
import com.football.scouting.note.entity.NoteCritere;
import com.football.scouting.note.repository.NoteCritereRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class NoteCritereControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName(
                            "football_scouting_note_test"
                    )
                    .withUsername("scouting_user")
                    .withPassword("scouting_pass");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteCritereRepository noteCritereRepository;

    @Autowired
    private RapportScoutingRepository rapportScoutingRepository;

    @Autowired
    private JoueurRepository joueurRepository;

    @BeforeEach
    void setUp() {
        noteCritereRepository.deleteAll();
        rapportScoutingRepository.deleteAll();
        joueurRepository.deleteAll();
    }

    @Test
    void createNote_shouldReturnCreatedNote()
            throws Exception {

        RapportScouting rapport = saveRapport();

        mockMvc.perform(
                        post("/api/notes")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request(
                                                                rapport.getId()
                                                        )
                                                )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(
                        jsonPath("$.rapportId")
                                .value(rapport.getId())
                )
                .andExpect(
                        jsonPath("$.critere")
                                .value("Technique")
                )
                .andExpect(
                        jsonPath("$.noteSur100")
                                .value(85)
                );
    }

    @Test
    void getAllNotes_shouldReturnNotes()
            throws Exception {

        RapportScouting rapport = saveRapport();

        noteCritereRepository.save(
                note(
                        rapport,
                        "Technique",
                        85
                )
        );

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].rapportId")
                                .value(rapport.getId())
                )
                .andExpect(
                        jsonPath("$[0].critere")
                                .value("Technique")
                )
                .andExpect(
                        jsonPath("$[0].noteSur100")
                                .value(85)
                );
    }

    @Test
    void getNoteById_shouldReturnNote_whenExists()
            throws Exception {

        NoteCritere saved =
                noteCritereRepository.save(
                        note(
                                saveRapport(),
                                "Passe",
                                88
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/notes/{id}",
                                saved.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(saved.getId())
                )
                .andExpect(
                        jsonPath("$.rapportId")
                                .value(
                                        saved.getRapport().getId()
                                )
                )
                .andExpect(
                        jsonPath("$.critere")
                                .value("Passe")
                )
                .andExpect(
                        jsonPath("$.noteSur100")
                                .value(88)
                );
    }

    @Test
    void getNotesByRapport_shouldReturnRapportNotes()
            throws Exception {

        RapportScouting rapport = saveRapport();

        noteCritereRepository.save(
                note(
                        rapport,
                        "Technique",
                        85
                )
        );

        noteCritereRepository.save(
                note(
                        rapport,
                        "Vitesse",
                        90
                )
        );

        mockMvc.perform(
                        get(
                                "/api/notes/rapport/{rapportId}",
                                rapport.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].critere")
                                .value("Technique")
                )
                .andExpect(
                        jsonPath("$[1].critere")
                                .value("Vitesse")
                );
    }

    @Test
    void updateNote_shouldReturnUpdatedNote()
            throws Exception {

        RapportScouting rapport = saveRapport();

        NoteCritere saved =
                noteCritereRepository.save(
                        note(
                                rapport,
                                "Technique",
                                70
                        )
                );

        NoteCritereRequest request =
                NoteCritereRequest.builder()
                        .rapportId(rapport.getId())
                        .critere("Vitesse")
                        .noteSur100(92)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/notes/{id}",
                                saved.getId()
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.rapportId")
                                .value(rapport.getId())
                )
                .andExpect(
                        jsonPath("$.critere")
                                .value("Vitesse")
                )
                .andExpect(
                        jsonPath("$.noteSur100")
                                .value(92)
                );
    }

    @Test
    void deleteNote_shouldReturn204_whenNoteExists()
            throws Exception {

        NoteCritere saved =
                noteCritereRepository.save(
                        note(
                                saveRapport(),
                                "Technique",
                                85
                        )
                );

        mockMvc.perform(
                        delete(
                                "/api/notes/{id}",
                                saved.getId()
                        )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void getNoteById_shouldReturn404_whenNotFound()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/notes/{id}",
                                99999L
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Note de critère introuvable avec l'id : 99999"
                                )
                );
    }

    @Test
    void createNote_shouldReturn404_whenRapportDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        post("/api/notes")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request(99999L)
                                                )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Rapport de scouting introuvable avec l'id : 99999"
                                )
                );
    }

    @Test
    void createNote_shouldReturn400_whenValidationFails()
            throws Exception {

        NoteCritereRequest request =
                NoteCritereRequest.builder()
                        .rapportId(null)
                        .critere(" ")
                        .noteSur100(101)
                        .build();

        mockMvc.perform(
                        post("/api/notes")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors",
                                hasKey("rapportId")
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors",
                                hasKey("critere")
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors",
                                hasKey("noteSur100")
                        )
                );
    }

    private NoteCritereRequest request(
            Long rapportId
    ) {
        return NoteCritereRequest.builder()
                .rapportId(rapportId)
                .critere("Technique")
                .noteSur100(85)
                .build();
    }

    private NoteCritere note(
            RapportScouting rapport,
            String critere,
            Integer noteSur100
    ) {
        return NoteCritere.builder()
                .rapport(rapport)
                .critere(critere)
                .noteSur100(noteSur100)
                .build();
    }

    private RapportScouting saveRapport() {
        Joueur joueur =
                joueurRepository.save(
                        Joueur.builder()
                                .nom("Mbappé")
                                .prenom("Kylian")
                                .postePrincipal("Milieu")
                                .build()
                );

        return rapportScoutingRepository.save(
                RapportScouting.builder()
                        .joueur(joueur)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        7,
                                        15
                                )
                        )
                        .matchObserve(
                                "Arsenal FC - Chelsea FC"
                        )
                        .commentaireGeneral(
                                "Bonne vision du jeu."
                        )
                        .recommandation("À suivre")
                        .scoutName("Jean Scout")
                        .build()
        );
    }

    @Test
    void createNote_shouldCalculateScoreGlobal()
            throws Exception {

        RapportScouting rapport = saveRapport();

        mockMvc.perform(
                        post("/api/notes")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                NoteCritereRequest.builder()
                                                        .rapportId(
                                                                rapport.getId()
                                                        )
                                                        .critere("Technique")
                                                        .noteSur100(80)
                                                        .build()
                                        )
                                )
                )
                .andExpect(status().isCreated());

        RapportScouting updatedRapport =
                rapportScoutingRepository
                        .findById(rapport.getId())
                        .orElseThrow();

        assertEquals(
                80,
                updatedRapport.getScoreGlobal()
        );
    }

    @Test
    void createSecondNote_shouldCalculateRoundedAverage()
            throws Exception {

        RapportScouting rapport = saveRapport();

        noteCritereRepository.save(
                NoteCritere.builder()
                        .rapport(rapport)
                        .critere("Technique")
                        .noteSur100(80)
                        .build()
        );

        mockMvc.perform(
                        post("/api/notes")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                NoteCritereRequest.builder()
                                                        .rapportId(
                                                                rapport.getId()
                                                        )
                                                        .critere("Vitesse")
                                                        .noteSur100(91)
                                                        .build()
                                        )
                                )
                )
                .andExpect(status().isCreated());

        /*
         * (80 + 91) / 2 = 85.5
         * Math.round donne 86.
         */
        RapportScouting updatedRapport =
                rapportScoutingRepository
                        .findById(rapport.getId())
                        .orElseThrow();

        assertEquals(
                86,
                updatedRapport.getScoreGlobal()
        );
    }

    @Test
    void updateNote_shouldRecalculateScoreGlobal()
            throws Exception {

        RapportScouting rapport = saveRapport();

        NoteCritere premiereNote =
                noteCritereRepository.save(
                        NoteCritere.builder()
                                .rapport(rapport)
                                .critere("Technique")
                                .noteSur100(60)
                                .build()
                );

        noteCritereRepository.save(
                NoteCritere.builder()
                        .rapport(rapport)
                        .critere("Vitesse")
                        .noteSur100(80)
                        .build()
        );

        NoteCritereRequest request =
                NoteCritereRequest.builder()
                        .rapportId(rapport.getId())
                        .critere("Technique")
                        .noteSur100(100)
                        .build();

        mockMvc.perform(
                        put(
                                "/api/notes/{id}",
                                premiereNote.getId()
                        )
                                .contentType("application/json")
                                .content(
                                        objectMapper
                                                .writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        /*
         * (100 + 80) / 2 = 90.
         */
        RapportScouting updatedRapport =
                rapportScoutingRepository
                        .findById(rapport.getId())
                        .orElseThrow();

        assertEquals(
                90,
                updatedRapport.getScoreGlobal()
        );
    }

    @Test
    void deleteNote_shouldRecalculateScoreGlobal()
            throws Exception {

        RapportScouting rapport = saveRapport();

        noteCritereRepository.save(
                NoteCritere.builder()
                        .rapport(rapport)
                        .critere("Technique")
                        .noteSur100(70)
                        .build()
        );

        NoteCritere noteToDelete =
                noteCritereRepository.save(
                        NoteCritere.builder()
                                .rapport(rapport)
                                .critere("Vitesse")
                                .noteSur100(90)
                                .build()
                );

        mockMvc.perform(
                        delete(
                                "/api/notes/{id}",
                                noteToDelete.getId()
                        )
                )
                .andExpect(status().isNoContent());

        RapportScouting updatedRapport =
                rapportScoutingRepository
                        .findById(rapport.getId())
                        .orElseThrow();

        assertEquals(
                70,
                updatedRapport.getScoreGlobal()
        );
    }

    @Test
    void deleteLastNote_shouldSetScoreGlobalToNull()
            throws Exception {

        RapportScouting rapport = saveRapport();

        NoteCritere note =
                noteCritereRepository.save(
                        NoteCritere.builder()
                                .rapport(rapport)
                                .critere("Technique")
                                .noteSur100(85)
                                .build()
                );

        rapport.setScoreGlobal(85);
        rapportScoutingRepository.save(rapport);

        mockMvc.perform(
                        delete(
                                "/api/notes/{id}",
                                note.getId()
                        )
                )
                .andExpect(status().isNoContent());

        RapportScouting updatedRapport =
                rapportScoutingRepository
                        .findById(rapport.getId())
                        .orElseThrow();

        assertNull(updatedRapport.getScoreGlobal());
    }
}
