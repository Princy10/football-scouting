package com.football.scouting.joueur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
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
class JoueurControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("football_scouting_joueur_test")
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
    private JoueurRepository joueurRepository;

    @Autowired
    private ClubRepository clubRepository;

    @BeforeEach
    void setUp() {
        joueurRepository.deleteAll();
        clubRepository.deleteAll();
    }

    @Test
    void createJoueur_shouldReturnCreatedJoueur() throws Exception {
        Club club = saveClub();

        mockMvc.perform(post("/api/joueurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(club.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nom").value("Rakoto"))
                .andExpect(jsonPath("$.postePrincipal").value("Milieu"))
                .andExpect(jsonPath("$.clubId").value(club.getId()));
    }

    @Test
    void getAllJoueurs_shouldReturnJoueurs() throws Exception {
        joueurRepository.save(joueur(null));

        mockMvc.perform(get("/api/joueurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Rakoto"));
    }

    @Test
    void getJoueurById_shouldReturnJoueur_whenExists() throws Exception {
        Joueur saved = joueurRepository.save(joueur(saveClub()));

        mockMvc.perform(get("/api/joueurs/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.clubId").value(saved.getClub().getId()));
    }

    @Test
    void updateJoueur_shouldReturnUpdatedJoueur() throws Exception {
        Joueur saved = joueurRepository.save(joueur(null));
        JoueurRequest request = request(null);
        request.setNom("Rakotoarisoa");
        request.setPostePrincipal("Attaquant");

        mockMvc.perform(put("/api/joueurs/{id}", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Rakotoarisoa"))
                .andExpect(jsonPath("$.postePrincipal").value("Attaquant"));
    }

    @Test
    void deleteJoueur_shouldReturn204_whenJoueurExists() throws Exception {
        Joueur saved = joueurRepository.save(joueur(null));

        mockMvc.perform(delete("/api/joueurs/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getJoueurById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/joueurs/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Joueur introuvable avec l'id : 99999"));
    }

    @Test
    void createJoueur_shouldReturn404_whenClubDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/joueurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(99999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Club introuvable avec l'id : 99999"));
    }

    @Test
    void createJoueur_shouldReturn400_whenValidationFails() throws Exception {
        JoueurRequest request = request(null);
        request.setNom("");
        request.setPostePrincipal("");

        mockMvc.perform(post("/api/joueurs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", hasKey("nom")))
                .andExpect(jsonPath("$.validationErrors", hasKey("postePrincipal")));
    }

    private JoueurRequest request(Long clubId) {
        return JoueurRequest.builder()
                .nom("Rakoto")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(2000, 1, 15))
                .nationalite("Malagasy")
                .postePrincipal("Milieu")
                .piedFort("Droit")
                .taille(178)
                .poids(72)
                .clubId(clubId)
                .build();
    }

    private Joueur joueur(Club club) {
        return Joueur.builder()
                .nom("Rakoto")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(2000, 1, 15))
                .nationalite("Malagasy")
                .postePrincipal("Milieu")
                .piedFort("Droit")
                .taille(178)
                .poids(72)
                .club(club)
                .build();
    }

    private Club saveClub() {
        return clubRepository.save(Club.builder()
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build());
    }
}
