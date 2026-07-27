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
                .andExpect(jsonPath("$.nom").value("Mbappé"))
                .andExpect(jsonPath("$.postePrincipal").value("Milieu"))
                .andExpect(jsonPath("$.clubId").value(club.getId()));
    }

    @Test
    void getAllJoueurs_shouldReturnPaginatedJoueurs()
            throws Exception {

        Joueur premier = joueur(null);
        premier.setNom("Mbappé");
        premier.setPrenom("Kylian");

        Joueur deuxieme = joueur(null);
        deuxieme.setNom("Wirtz");
        deuxieme.setPrenom("Florian");

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Mbappé")
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
    void getAllJoueurs_shouldSortPlayersByNameDescending()
            throws Exception {

        Joueur premier = joueur(null);
        premier.setNom("Mbappé");
        premier.setPrenom("Kylian");

        Joueur deuxieme = joueur(null);
        deuxieme.setNom("Wirtz");
        deuxieme.setPrenom("Florian");

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "nom")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Wirtz")
                )
                .andExpect(
                        jsonPath("$.content[1].nom")
                                .value("Mbappé")
                );
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
    void getAllJoueurs_shouldSearchByName()
            throws Exception {

        Joueur premier = joueur(null);
        premier.setNom("Mbappé");
        premier.setPrenom("Kylian");

        Joueur deuxieme = joueur(null);
        deuxieme.setNom("Wirtz");
        deuxieme.setPrenom("Florian");

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "nom")
                                .param("direction", "asc")
                                .param("search", "mbapp")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Mbappé")
                )
                .andExpect(
                        jsonPath("$.content[0].prenom")
                                .value("Kylian")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldSearchByFirstName()
            throws Exception {

        Joueur premier = joueur(null);
        premier.setNom("Mbappé");
        premier.setPrenom("Kylian");

        Joueur deuxieme = joueur(null);
        deuxieme.setNom("Wirtz");
        deuxieme.setPrenom("Florian");

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("search", "florian")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Wirtz")
                )
                .andExpect(
                        jsonPath("$.content[0].prenom")
                                .value("Florian")
                );
    }

    @Test
    void getAllJoueurs_shouldFilterByClub()
            throws Exception {

        Club premierClub =
                clubRepository.save(
                        Club.builder()
                                .nom("Real Madrid CF")
                                .pays("Espagne")
                                .ville("Madrid")
                                .division("La Liga")
                                .build()
                );

        Club deuxiemeClub =
                clubRepository.save(
                        Club.builder()
                                .nom("Liverpool FC")
                                .pays("Angleterre")
                                .ville("Liverpool")
                                .division("Premier League")
                                .build()
                );

        Joueur premier = joueur(premierClub);
        premier.setNom("Mbappé");
        premier.setPrenom("Kylian");

        Joueur deuxieme = joueur(deuxiemeClub);
        deuxieme.setNom("Salah");
        deuxieme.setPrenom("Mohamed");

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "clubId",
                                        premierClub.getId().toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Mbappé")
                )
                .andExpect(
                        jsonPath("$.content[0].clubId")
                                .value(premierClub.getId())
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldCombineSearchAndClubFilter()
            throws Exception {

        Club realMadrid =
                clubRepository.save(
                        Club.builder()
                                .nom("Real Madrid CF")
                                .pays("Espagne")
                                .ville("Madrid")
                                .division("La Liga")
                                .build()
                );

        Club autreClub =
                clubRepository.save(
                        Club.builder()
                                .nom("Paris Saint-Germain")
                                .pays("France")
                                .ville("Paris")
                                .division("Ligue 1")
                                .build()
                );

        Joueur kylianMadrid = joueur(realMadrid);
        kylianMadrid.setNom("Mbappé");
        kylianMadrid.setPrenom("Kylian");

        Joueur kylianParis = joueur(autreClub);
        kylianParis.setNom("Test");
        kylianParis.setPrenom("Kylian");

        joueurRepository.save(kylianMadrid);
        joueurRepository.save(kylianParis);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("search", "kylian")
                                .param(
                                        "clubId",
                                        realMadrid.getId().toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Mbappé")
                )
                .andExpect(
                        jsonPath("$.content[0].clubId")
                                .value(realMadrid.getId())
                );
    }

    @Test
    void updateJoueur_shouldReturnUpdatedJoueur() throws Exception {
        Joueur saved = joueurRepository.save(joueur(null));
        JoueurRequest request = request(null);
        request.setNom("Hernández");
        request.setPostePrincipal("Attaquant");

        mockMvc.perform(put("/api/joueurs/{id}", saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Hernández"))
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
                .nom("Mbappé")
                .prenom("Kylian")
                .dateNaissance(LocalDate.of(2000, 1, 15))
                .nationalite("Française")
                .postePrincipal("Milieu")
                .piedFort("Droit")
                .taille(178)
                .poids(72)
                .clubId(clubId)
                .build();
    }

    private Joueur joueur(Club club) {
        return Joueur.builder()
                .nom("Mbappé")
                .prenom("Kylian")
                .dateNaissance(LocalDate.of(2000, 1, 15))
                .nationalite("Française")
                .postePrincipal("Milieu")
                .piedFort("Droit")
                .taille(178)
                .poids(72)
                .club(club)
                .build();
    }

    private Club saveClub() {
        return clubRepository.save(Club.builder()
                .nom("Arsenal FC")
                .pays("Angleterre")
                .ville("Londres")
                .division("D1")
                .build());
    }
}
