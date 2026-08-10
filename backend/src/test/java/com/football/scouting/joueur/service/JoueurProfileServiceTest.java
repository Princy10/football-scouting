package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.joueur.dto.profile.JoueurProfileResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.note.entity.NoteCritere;
import com.football.scouting.note.repository.NoteCritereRepository;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoueurProfileServiceTest {

    @Mock
    private JoueurRepository joueurRepository;

    @Mock
    private RapportScoutingRepository
            rapportScoutingRepository;

    @Mock
    private NoteCritereRepository
            noteCritereRepository;

    private JoueurProfileService service;

    @BeforeEach
    void setUp() {
        service =
                new JoueurProfileService(
                        joueurRepository,
                        rapportScoutingRepository,
                        noteCritereRepository
                );
    }

    @Test
    void getProfile_shouldReturnAggregatedProfile() {
        Club club =
                Club.builder()
                        .id(4L)
                        .nom("Real Madrid CF")
                        .pays("Espagne")
                        .ville("Madrid")
                        .division("La Liga")
                        .build();

        Joueur joueur =
                Joueur.builder()
                        .id(32L)
                        .nom("Mbappé")
                        .prenom("Kylian")
                        .nationalite("France")
                        .postePrincipal("Attaquant")
                        .club(club)
                        .build();

        RapportScouting rapport1 =
                RapportScouting.builder()
                        .id(1L)
                        .joueur(joueur)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        5,
                                        20
                                )
                        )
                        .scoreGlobal(90)
                        .recommandation(
                                "RECOMMANDE"
                        )
                        .build();

        RapportScouting rapport2 =
                RapportScouting.builder()
                        .id(2L)
                        .joueur(joueur)
                        .dateObservation(
                                LocalDate.of(
                                        2026,
                                        5,
                                        10
                                )
                        )
                        .scoreGlobal(80)
                        .recommandation(
                                "A_SUIVRE"
                        )
                        .build();

        NoteCritere note1 =
                NoteCritere.builder()
                        .id(1L)
                        .rapport(rapport1)
                        .critere("Finition")
                        .noteSur100(90)
                        .build();

        NoteCritere note2 =
                NoteCritere.builder()
                        .id(2L)
                        .rapport(rapport2)
                        .critere("Finition")
                        .noteSur100(80)
                        .build();

        NoteCritere note3 =
                NoteCritere.builder()
                        .id(3L)
                        .rapport(rapport1)
                        .critere("Vitesse")
                        .noteSur100(95)
                        .build();

        when(
                joueurRepository
                        .findProfileJoueurById(32L)
        ).thenReturn(
                Optional.of(joueur)
        );

        when(
                rapportScoutingRepository
                        .findByJoueur_IdOrderByDateObservationDesc(
                                32L
                        )
        ).thenReturn(
                List.of(
                        rapport1,
                        rapport2
                )
        );

        when(
                noteCritereRepository
                        .findByRapport_IdInOrderByRapport_IdAscIdAsc(
                                List.of(1L, 2L)
                        )
        ).thenReturn(
                List.of(
                        note1,
                        note3,
                        note2
                )
        );

        JoueurProfileResponse response =
                service.getProfile(32L);

        assertEquals(
                "Mbappé",
                response.getJoueur().getNom()
        );

        assertEquals(
                "Real Madrid CF",
                response.getClub().getNom()
        );

        assertEquals(
                2,
                response.getStatistiques()
                        .getNombreRapports()
        );

        assertEquals(
                3,
                response.getStatistiques()
                        .getNombreNotes()
        );

        assertEquals(
                2,
                response.getStatistiques()
                        .getNombreCriteresDistincts()
        );

        assertEquals(
                85.0,
                response.getStatistiques()
                        .getScoreMoyen()
        );

        assertEquals(
                80,
                response.getStatistiques()
                        .getScoreMinimum()
        );

        assertEquals(
                90,
                response.getStatistiques()
                        .getScoreMaximum()
        );

        assertEquals(
                90,
                response.getStatistiques()
                        .getDernierScore()
        );

        assertEquals(
                2,
                response.getRapports().size()
        );

        assertEquals(
                2,
                response.getCriteres().size()
        );

        /*
         * Finition :
         * (90 + 80) / 2 = 85
         */
        assertEquals(
                85.0,
                response.getCriteres()
                        .stream()
                        .filter(c ->
                                c.getCritere()
                                        .equals("Finition")
                        )
                        .findFirst()
                        .orElseThrow()
                        .getMoyenne()
        );
    }

    @Test
    void getProfile_shouldReturnEmptyStats_whenNoReportExists() {
        Joueur joueur =
                Joueur.builder()
                        .id(10L)
                        .nom("Test")
                        .prenom("Player")
                        .postePrincipal("Milieu")
                        .build();

        when(
                joueurRepository
                        .findProfileJoueurById(10L)
        ).thenReturn(
                Optional.of(joueur)
        );

        when(
                rapportScoutingRepository
                        .findByJoueur_IdOrderByDateObservationDesc(
                                10L
                        )
        ).thenReturn(
                List.of()
        );

        JoueurProfileResponse response =
                service.getProfile(10L);

        assertEquals(
                0,
                response.getStatistiques()
                        .getNombreRapports()
        );

        assertEquals(
                0,
                response.getStatistiques()
                        .getNombreNotes()
        );

        assertEquals(
                0,
                response.getRapports().size()
        );

        assertEquals(
                0,
                response.getCriteres().size()
        );
    }
}