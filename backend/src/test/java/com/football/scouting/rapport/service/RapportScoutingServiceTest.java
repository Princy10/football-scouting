package com.football.scouting.rapport.service;

import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.common.exception.InvalidFilterException;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.rapport.dto.RapportScoutingFilterRequest;
import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        assertEquals("Arsenal FC - Chelsea FC", response.getMatchObserve());
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
    void getAllRapports_shouldReturnPaginatedResponses() {
        RapportScouting premier =
                rapport(1L, joueur(10L));

        RapportScouting deuxieme =
                rapport(2L, joueur(20L));

        when(
                rapportScoutingRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(
                                premier,
                                deuxieme
                        )
                )
        );

        RapportScoutingFilterRequest filters =
                new RapportScoutingFilterRequest();

        PageResponse<RapportScoutingResponse> response =
                rapportScoutingService
                        .getAllRapports(filters);

        assertEquals(
                2,
                response.getContent().size()
        );

        assertEquals(
                10L,
                response.getContent()
                        .get(0)
                        .getJoueurId()
        );

        assertEquals(
                20L,
                response.getContent()
                        .get(1)
                        .getJoueurId()
        );

        verify(rapportScoutingRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
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

    @Test
    void getAllRapports_shouldApplyScoreDescendingSort() {
        when(
                rapportScoutingRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of())
        );

        RapportScoutingFilterRequest filters =
                new RapportScoutingFilterRequest();

        filters.setSortBy("scoreGlobal");
        filters.setDirection("desc");

        rapportScoutingService
                .getAllRapports(filters);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(
                        Pageable.class
                );

        verify(rapportScoutingRepository)
                .findAll(
                        any(Specification.class),
                        pageableCaptor.capture()
                );

        Pageable pageable =
                pageableCaptor.getValue();

        Sort.Order order =
                pageable.getSort()
                        .getOrderFor("scoreGlobal");

        assertNotNull(order);

        assertEquals(
                Sort.Direction.DESC,
                order.getDirection()
        );
    }

    @Test
    void getAllRapports_shouldUseAllFilters() {
        Joueur joueur = joueur(32L);

        RapportScouting rapport =
                rapport(1L, joueur);

        rapport.setScoreGlobal(88);
        rapport.setRecommandation("RECOMMANDE");
        rapport.setScoutName("Alice Dupont");
        rapport.setDateObservation(
                LocalDate.of(2026, 5, 15)
        );

        when(
                rapportScoutingRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of(rapport))
        );

        RapportScoutingFilterRequest filters =
                new RapportScoutingFilterRequest();

        filters.setSearch("analyse");
        filters.setJoueurId(32L);
        filters.setScoreMin(80);
        filters.setScoreMax(90);
        filters.setRecommandation("RECOMMANDE");
        filters.setScout("alice");
        filters.setDateObservationMin(
                LocalDate.of(2026, 5, 1)
        );
        filters.setDateObservationMax(
                LocalDate.of(2026, 5, 31)
        );

        PageResponse<RapportScoutingResponse> response =
                rapportScoutingService
                        .getAllRapports(filters);

        assertEquals(
                1,
                response.getContent().size()
        );

        assertEquals(
                88,
                response.getContent()
                        .getFirst()
                        .getScoreGlobal()
        );

        assertEquals(
                "Alice Dupont",
                response.getContent()
                        .getFirst()
                        .getScoutName()
        );

        assertEquals(
                32L,
                response.getContent()
                        .getFirst()
                        .getJoueurId()
        );
    }

    @Test
    void getAllRapports_shouldRejectInvalidScoreRange() {
        RapportScoutingFilterRequest filters =
                new RapportScoutingFilterRequest();

        filters.setScoreMin(90);
        filters.setScoreMax(70);

        InvalidFilterException exception =
                assertThrows(
                        InvalidFilterException.class,
                        () -> rapportScoutingService
                                .getAllRapports(filters)
                );

        assertEquals(
                "Le score minimal ne doit pas être supérieur "
                        + "au score maximal.",
                exception.getMessage()
        );

        verifyNoInteractions(
                rapportScoutingRepository
        );
    }

    @Test
    void getAllRapports_shouldRejectInvalidDateRange() {
        RapportScoutingFilterRequest filters =
                new RapportScoutingFilterRequest();

        filters.setDateObservationMin(
                LocalDate.of(2026, 6, 1)
        );

        filters.setDateObservationMax(
                LocalDate.of(2026, 5, 1)
        );

        InvalidFilterException exception =
                assertThrows(
                        InvalidFilterException.class,
                        () -> rapportScoutingService
                                .getAllRapports(filters)
                );

        assertEquals(
                "La date d'observation minimale ne doit pas "
                        + "être postérieure à la date maximale.",
                exception.getMessage()
        );
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

    private RapportScouting rapport(Long id, Joueur joueur) {
        return RapportScouting.builder()
                .id(id)
                .joueur(joueur)
                .dateObservation(LocalDate.of(2026, 7, 15))
                .matchObserve("Arsenal FC - Chelsea FC")
                .commentaireGeneral("Bonne vision du jeu et excellente qualité de passe.")
                .recommandation("À suivre")
                .scoreGlobal(82)
                .scoutName("Jean Scout")
                .build();
    }

    private Joueur joueur(Long id) {
        return Joueur.builder()
                .id(id)
                .nom("Mbappé")
                .postePrincipal("Milieu")
                .build();
    }
}
