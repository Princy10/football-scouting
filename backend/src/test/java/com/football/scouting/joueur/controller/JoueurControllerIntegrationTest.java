package com.football.scouting.joueur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
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

    @Autowired
    private RapportScoutingRepository rapportScoutingRepository;

    @Autowired
    private NoteCritereRepository noteCritereRepository;

    @BeforeEach
    void setUp() {
        noteCritereRepository.deleteAll();
        rapportScoutingRepository.deleteAll();
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

    @Test
    void getAllJoueurs_shouldFilterByPoste()
            throws Exception {

        Joueur attaquant = joueur(null);
        attaquant.setNom("Mbappé");
        attaquant.setPrenom("Kylian");
        attaquant.setPostePrincipal("Attaquant");

        Joueur milieu = joueur(null);
        milieu.setNom("Wirtz");
        milieu.setPrenom("Florian");
        milieu.setPostePrincipal("Milieu");

        joueurRepository.save(attaquant);
        joueurRepository.save(milieu);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "poste",
                                        "attaquant"
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
                        jsonPath(
                                "$.content[0].postePrincipal"
                        )
                                .value("Attaquant")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldCombineAllFilters()
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

        Club paris =
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
        kylianMadrid.setPostePrincipal("Attaquant");
        kylianMadrid.setNationalite("France");

        Joueur kylianMilieu = joueur(realMadrid);
        kylianMilieu.setNom("Test");
        kylianMilieu.setPrenom("Kylian");
        kylianMilieu.setPostePrincipal("Milieu");
        kylianMilieu.setNationalite("France");

        Joueur kylianParis = joueur(paris);
        kylianParis.setNom("Autre");
        kylianParis.setPrenom("Kylian");
        kylianParis.setPostePrincipal("Attaquant");
        kylianParis.setNationalite("Belgique");

        joueurRepository.save(kylianMadrid);
        joueurRepository.save(kylianMilieu);
        joueurRepository.save(kylianParis);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("search", "kylian")
                                .param(
                                        "clubId",
                                        realMadrid
                                                .getId()
                                                .toString()
                                )
                                .param(
                                        "poste",
                                        "Attaquant"
                                )
                                .param("nationalite", "France")
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
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].postePrincipal"
                        )
                                .value("Attaquant")
                )
                .andExpect(
                        jsonPath("$.content[0].nationalite")
                                .value("France")
                );
    }

    @Test
    void getAllJoueurs_shouldFilterByNationalite()
            throws Exception {

        Joueur joueurFrancais = joueur(null);
        joueurFrancais.setNom("Mbappé");
        joueurFrancais.setPrenom("Kylian");
        joueurFrancais.setNationalite("France");
        joueurFrancais.setPostePrincipal("Attaquant");

        Joueur joueurAllemand = joueur(null);
        joueurAllemand.setNom("Wirtz");
        joueurAllemand.setPrenom("Florian");
        joueurAllemand.setNationalite("Allemagne");
        joueurAllemand.setPostePrincipal("Milieu");

        joueurRepository.save(joueurFrancais);
        joueurRepository.save(joueurAllemand);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "nationalite",
                                        "france"
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
                        jsonPath(
                                "$.content[0].nationalite"
                        )
                                .value("France")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldFilterByPhysicalCriteria()
            throws Exception {

        Joueur matchingPlayer = joueur(null);
        matchingPlayer.setNom("Alpha");
        matchingPlayer.setPrenom("Test");
        matchingPlayer.setPiedFort("Droit");
        matchingPlayer.setTaille(180);
        matchingPlayer.setPoids(76);
        matchingPlayer.setDateNaissance(
                LocalDate.of(2000, 5, 10)
        );

        Joueur otherPlayer = joueur(null);
        otherPlayer.setNom("Beta");
        otherPlayer.setPrenom("Test");
        otherPlayer.setPiedFort("Gauche");
        otherPlayer.setTaille(170);
        otherPlayer.setPoids(65);
        otherPlayer.setDateNaissance(
                LocalDate.of(1995, 4, 15)
        );

        joueurRepository.save(matchingPlayer);
        joueurRepository.save(otherPlayer);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("piedFort", "droit")
                                .param("tailleMin", "175")
                                .param("tailleMax", "185")
                                .param("poidsMin", "70")
                                .param("poidsMax", "80")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Alpha")
                )
                .andExpect(
                        jsonPath("$.content[0].piedFort")
                                .value("Droit")
                )
                .andExpect(
                        jsonPath("$.content[0].taille")
                                .value(180)
                )
                .andExpect(
                        jsonPath("$.content[0].poids")
                                .value(76)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldFilterByBirthDateRange()
            throws Exception {

        Joueur premier = joueur(null);
        premier.setNom("Alpha");
        premier.setDateNaissance(
                LocalDate.of(2001, 6, 15)
        );

        Joueur deuxieme = joueur(null);
        deuxieme.setNom("Beta");
        deuxieme.setDateNaissance(
                LocalDate.of(1995, 3, 20)
        );

        joueurRepository.save(premier);
        joueurRepository.save(deuxieme);

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "dateNaissanceMin",
                                        "2000-01-01"
                                )
                                .param(
                                        "dateNaissanceMax",
                                        "2002-12-31"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].nom")
                                .value("Alpha")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].dateNaissance"
                        )
                                .value("2001-06-15")
                );
    }

    @Test
    void getAllJoueurs_shouldReturn400_whenRangeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/joueurs")
                                .param("tailleMin", "190")
                                .param("tailleMax", "175")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La taille minimale ne doit pas "
                                                + "être supérieure à la "
                                                + "taille maximale."
                                )
                );
    }

    @Test
    void getAllJoueurs_shouldFilterUsingScoutingReports()
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

        Joueur cible = joueur(realMadrid);
        cible.setNom("Mbappé");
        cible.setPrenom("Kylian");
        cible.setPostePrincipal("Attaquant");
        cible.setNationalite("France");

        Joueur autreJoueur = joueur(realMadrid);
        autreJoueur.setNom("Bellingham");
        autreJoueur.setPrenom("Jude");
        autreJoueur.setPostePrincipal("Milieu");
        autreJoueur.setNationalite("Angleterre");

        cible = joueurRepository.save(cible);
        autreJoueur =
                joueurRepository.save(autreJoueur);

        /*
         * Premier rapport correspondant pour Mbappé.
         */
        rapportScoutingRepository.save(
                RapportScouting.builder()
                        .joueur(cible)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        5,
                                        15
                                )
                        )
                        .matchObserve(
                                "Analyse offensive européenne"
                        )
                        .commentaireGeneral(
                                "Très bonne prestation."
                        )
                        .scoreGlobal(88)
                        .recommandation(
                                "RECOMMANDE"
                        )
                        .scoutName(
                                "Alice Dupont"
                        )
                        .build()
        );

        /*
         * Deuxième rapport correspondant pour le même joueur.
         * Il sert à vérifier que Mbappé ne sera pas dupliqué.
         */
        rapportScoutingRepository.save(
                RapportScouting.builder()
                        .joueur(cible)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        5,
                                        20
                                )
                        )
                        .matchObserve(
                                "Deuxième analyse"
                        )
                        .commentaireGeneral(
                                "Confirmation du potentiel."
                        )
                        .scoreGlobal(90)
                        .recommandation(
                                "RECOMMANDE"
                        )
                        .scoutName(
                                "Jean Scout"
                        )
                        .build()
        );

        /*
         * Rapport d'un autre joueur.
         */
        rapportScoutingRepository.save(
                RapportScouting.builder()
                        .joueur(autreJoueur)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        5,
                                        18
                                )
                        )
                        .matchObserve(
                                "Analyse du milieu"
                        )
                        .commentaireGeneral(
                                "Bonne maîtrise technique."
                        )
                        .scoreGlobal(92)
                        .recommandation(
                                "RECOMMANDE"
                        )
                        .scoutName(
                                "Alice Dupont"
                        )
                        .build()
        );

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "search",
                                        "kylian"
                                )
                                .param(
                                        "clubId",
                                        realMadrid
                                                .getId()
                                                .toString()
                                )
                                .param(
                                        "poste",
                                        "Attaquant"
                                )
                                .param(
                                        "nationalite",
                                        "France"
                                )
                                .param(
                                        "scoreGlobalMin",
                                        "80"
                                )
                                .param(
                                        "scoreGlobalMax",
                                        "90"
                                )
                                .param(
                                        "recommandationRapport",
                                        "recommande"
                                )
                                .param(
                                        "dateRapportMin",
                                        "2026-05-01"
                                )
                                .param(
                                        "dateRapportMax",
                                        "2026-05-31"
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
                        jsonPath("$.content[0].prenom")
                                .value("Kylian")
                )
                .andExpect(
                        jsonPath("$.content[0].postePrincipal")
                                .value("Attaquant")
                )
                .andExpect(
                        jsonPath("$.content[0].nationalite")
                                .value("France")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    @Test
    void getAllJoueurs_shouldReturn400_whenReportScoreRangeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "scoreGlobalMin",
                                        "90"
                                )
                                .param(
                                        "scoreGlobalMax",
                                        "70"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Le score global minimal ne doit "
                                                + "pas être supérieur au "
                                                + "score global maximal."
                                )
                );
    }

    @Test
    void getAllJoueurs_shouldReturn400_whenReportDateRangeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/joueurs")
                                .param(
                                        "dateRapportMin",
                                        "2026-06-01"
                                )
                                .param(
                                        "dateRapportMax",
                                        "2026-05-01"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La date minimale du rapport ne "
                                                + "doit pas être postérieure "
                                                + "à la date maximale."
                                )
                );
    }

    @Test
    void getJoueurProfile_shouldReturnCompleteProfile()
            throws Exception {

        Club club =
                clubRepository.save(
                        Club.builder()
                                .nom("Real Madrid CF")
                                .pays("Espagne")
                                .ville("Madrid")
                                .division("La Liga")
                                .build()
                );

        Joueur joueur = joueur(club);
        joueur.setNom("Mbappé");
        joueur.setPrenom("Kylian");
        joueur.setNationalite("France");
        joueur.setPostePrincipal("Attaquant");

        joueur =
                joueurRepository.save(joueur);

        RapportScouting rapport =
                rapportScoutingRepository.save(
                        RapportScouting.builder()
                                .joueur(joueur)
                                .dateObservation(
                                        LocalDate.of(
                                                2026,
                                                5,
                                                20
                                        )
                                )
                                .matchObserve(
                                        "Real Madrid - Barcelona"
                                )
                                .commentaireGeneral(
                                        "Très bonne prestation"
                                )
                                .recommandation(
                                        "RECOMMANDE"
                                )
                                .scoreGlobal(90)
                                .scoutName(
                                        "Alice Dupont"
                                )
                                .build()
                );

        noteCritereRepository.save(
                NoteCritere.builder()
                        .rapport(rapport)
                        .critere("Finition")
                        .noteSur100(92)
                        .build()
        );

        noteCritereRepository.save(
                NoteCritere.builder()
                        .rapport(rapport)
                        .critere("Vitesse")
                        .noteSur100(88)
                        .build()
        );

        mockMvc.perform(
                        get(
                                "/api/joueurs/{id}/profil",
                                joueur.getId()
                        )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.joueur.nom")
                                .value("Mbappé")
                )

                .andExpect(
                        jsonPath("$.club.nom")
                                .value(
                                        "Real Madrid CF"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.statistiques.nombreRapports"
                        )
                                .value(1)
                )

                .andExpect(
                        jsonPath(
                                "$.statistiques.nombreNotes"
                        )
                                .value(2)
                )

                .andExpect(
                        jsonPath(
                                "$.statistiques.scoreMoyen"
                        )
                                .value(90.0)
                )

                .andExpect(
                        jsonPath(
                                "$.criteres.length()"
                        )
                                .value(2)
                )

                .andExpect(
                        jsonPath(
                                "$.rapports.length()"
                        )
                                .value(1)
                )

                .andExpect(
                        jsonPath(
                                "$.rapports[0].notes.length()"
                        )
                                .value(2)
                );
    }

    @Test
    void getJoueurProfile_shouldReturn404_whenPlayerDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/joueurs/{id}/profil",
                                999999L
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Joueur introuvable avec l'id : 999999"
                                )
                );
    }
}
