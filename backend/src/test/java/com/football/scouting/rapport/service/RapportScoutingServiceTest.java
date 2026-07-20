package com.football.scouting.rapport.service;

import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RapportScoutingServiceTest {

    @Mock
    private RapportScoutingRepository rapportScoutingRepository;

    @Mock
    private JoueurRepository joueurRepository;

    @InjectMocks
    private RapportScoutingService rapportScoutingService;

    @Test
    void createRapport_shouldCreateAndReturnResponse() {
        Joueur joueur = joueur(1L);
        RapportScouting savedRapport = rapport(10L, joueur);

        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur));
        when(rapportScoutingRepository.save(any(RapportScouting.class))).thenReturn(savedRapport);

        RapportScoutingResponse response = rapportScoutingService.createRapport(request(1L));

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getJoueurId());
        assertEquals(LocalDate.of(2026, 7, 15), response.getDateObservation());
        assertEquals("Ajesaia - Elgeco Plus", response.getMatchObserve());
        assertEquals(82, response.getScoreGlobal());
        verify(joueurRepository).findById(1L);
        verify(rapportScoutingRepository).save(any(RapportScouting.class));
    }

    @Test
    void createRapport_shouldThrowException_whenJoueurDoesNotExist() {
        when(joueurRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> rapportScoutingService.createRapport(request(99L))
        );

        assertEquals("Joueur introuvable avec l'id : 99", exception.getMessage());
    }

    @Test
    void getAllRapports_shouldReturnResponses() {
        when(
                rapportScoutingRepository.findAllByOrderByCreatedAtDesc()
        ).thenReturn(List.of(
                rapport(1L, joueur(10L)),
                rapport(2L, joueur(20L))
        ));

        List<RapportScoutingResponse> responses =
                rapportScoutingService.getAllRapports();

        assertEquals(2, responses.size());
        assertEquals(10L, responses.get(0).getJoueurId());
        assertEquals(20L, responses.get(1).getJoueurId());

        verify(
                rapportScoutingRepository
        ).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getRapportById_shouldReturnResponse_whenRapportExists() {
        when(rapportScoutingRepository.findById(1L))
                .thenReturn(Optional.of(rapport(1L, joueur(2L))));

        RapportScoutingResponse response = rapportScoutingService.getRapportById(1L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getJoueurId());
    }

    @Test
    void getRapportById_shouldThrowException_whenRapportDoesNotExist() {
        when(rapportScoutingRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> rapportScoutingService.getRapportById(99L)
        );

        assertEquals("Rapport de scouting introuvable avec l'id : 99", exception.getMessage());
    }

    @Test
    void updateRapport_shouldUpdateAndReturnResponse() {
        RapportScouting existing = rapport(1L, joueur(1L));
        Joueur nouveauJoueur = joueur(2L);
        RapportScoutingRequest request = request(2L);
        request.setRecommandation("À recruter");

        when(rapportScoutingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(joueurRepository.findById(2L)).thenReturn(Optional.of(nouveauJoueur));
        when(rapportScoutingRepository.save(existing)).thenReturn(existing);

        RapportScoutingResponse response = rapportScoutingService.updateRapport(1L, request);

        assertEquals(2L, response.getJoueurId());
        assertEquals("À recruter", response.getRecommandation());
        assertEquals(82, response.getScoreGlobal());
        verify(rapportScoutingRepository).save(existing);
    }

    @Test
    void deleteRapport_shouldDeleteRapport_whenRapportExists() {
        RapportScouting existing = rapport(1L, joueur(1L));
        when(rapportScoutingRepository.findById(1L)).thenReturn(Optional.of(existing));

        rapportScoutingService.deleteRapport(1L);

        verify(rapportScoutingRepository).delete(existing);
    }

    private RapportScoutingRequest request(Long joueurId) {
        return RapportScoutingRequest.builder()
                .joueurId(joueurId)
                .dateObservation(LocalDate.of(2026, 7, 15))
                .matchObserve("Ajesaia - Elgeco Plus")
                .commentaireGeneral("Bonne vision du jeu et excellente qualité de passe.")
                .recommandation("À suivre")
                .scoutName("Jean Scout")
                .build();
    }

    private RapportScouting rapport(Long id, Joueur joueur) {
        return RapportScouting.builder()
                .id(id)
                .joueur(joueur)
                .dateObservation(LocalDate.of(2026, 7, 15))
                .matchObserve("Ajesaia - Elgeco Plus")
                .commentaireGeneral("Bonne vision du jeu et excellente qualité de passe.")
                .recommandation("À suivre")
                .scoreGlobal(82)
                .scoutName("Jean Scout")
                .build();
    }

    private Joueur joueur(Long id) {
        return Joueur.builder()
                .id(id)
                .nom("Rakoto")
                .postePrincipal("Milieu")
                .build();
    }
}
