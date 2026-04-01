package com.football.scouting.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.scouting.club.dto.ClubRequest;
import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
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

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ClubControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("football_scouting_test")
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
    private ClubRepository clubRepository;

    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
    }

    @Test
    void createClub_shouldReturnCreatedClub() throws Exception {
        ClubRequest request = ClubRequest.builder()
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build();

        mockMvc.perform(post("/api/clubs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nom").value("Ajesaia"))
                .andExpect(jsonPath("$.pays").value("Madagascar"))
                .andExpect(jsonPath("$.ville").value("Antananarivo"))
                .andExpect(jsonPath("$.division").value("D1"));
    }

    @Test
    void getClubById_shouldReturnClub_whenExists() throws Exception {
        Club savedClub = clubRepository.save(
                Club.builder()
                        .nom("Ajesaia")
                        .pays("Madagascar")
                        .ville("Antananarivo")
                        .division("D1")
                        .build()
        );

        mockMvc.perform(get("/api/clubs/{id}", savedClub.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedClub.getId()))
                .andExpect(jsonPath("$.nom").value("Ajesaia"))
                .andExpect(jsonPath("$.pays").value("Madagascar"));
    }

    @Test
    void getClubById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/clubs/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Club introuvable avec l'id : 99999"))
                .andExpect(jsonPath("$.path").value("/api/clubs/99999"));
    }

    @Test
    void createClub_shouldReturn400_whenValidationFails() throws Exception {
        ClubRequest request = ClubRequest.builder()
                .nom("")
                .pays("")
                .ville("Antananarivo")
                .division("D1")
                .build();

        mockMvc.perform(post("/api/clubs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erreur de validation sur la requête."))
                .andExpect(jsonPath("$.path").value("/api/clubs"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors", hasKey("nom")))
                .andExpect(jsonPath("$.validationErrors", hasKey("pays")));
    }

    @Test
    void deleteClub_shouldReturn204_whenClubExists() throws Exception {
        Club savedClub = clubRepository.save(
                Club.builder()
                        .nom("Ajesaia")
                        .pays("Madagascar")
                        .ville("Antananarivo")
                        .division("D1")
                        .build()
        );

        mockMvc.perform(delete("/api/clubs/{id}", savedClub.getId()))
                .andExpect(status().isNoContent());
    }
}