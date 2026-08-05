package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.common.exception.InvalidFilterException;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.dto.JoueurFilterRequest;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoueurServiceTest {

    @Mock
    private JoueurRepository joueurRepository;

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private JoueurService joueurService;

    @Test
    void createJoueur_shouldCreateAndReturnResponse_withClub() {
        Club club = club(1L);
        JoueurRequest request = request(1L);
        Joueur savedJoueur = joueur(10L, club);

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
        when(joueurRepository.save(any(Joueur.class))).thenReturn(savedJoueur);

        JoueurResponse response = joueurService.createJoueur(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Mbappé", response.getNom());
        assertEquals("Milieu", response.getPostePrincipal());
        assertEquals(1L, response.getClubId());
        verify(clubRepository).findById(1L);
        verify(joueurRepository).save(any(Joueur.class));
    }

    @Test
    void createJoueur_shouldCreateJoueur_withoutClub() {
        JoueurRequest request = request(null);
        Joueur savedJoueur = joueur(10L, null);
        when(joueurRepository.save(any(Joueur.class))).thenReturn(savedJoueur);

        JoueurResponse response = joueurService.createJoueur(request);

        assertNull(response.getClubId());
        verify(joueurRepository).save(any(Joueur.class));
    }

    @Test
    void createJoueur_shouldThrowException_whenClubDoesNotExist() {
        when(clubRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> joueurService.createJoueur(request(99L))
        );

        assertEquals("Club introuvable avec l'id : 99", exception.getMessage());
    }

    @Test
    void getAllJoueurs_shouldReturnPaginatedResponses() {
        when(
                joueurRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(
                                joueur(1L, null),
                                joueur(2L, club(3L))
                        )
                )
        );

        JoueurFilterRequest filters =
                new JoueurFilterRequest();

        PageResponse<JoueurResponse> response =
                joueurService.getAllJoueurs(filters);

        assertEquals(2, response.getContent().size());
        assertEquals(
                2,
                response.getTotalElements()
        );
        assertEquals(
                1,
                response.getTotalPages()
        );
        assertEquals(
                0,
                response.getPage()
        );

        assertNull(
                response.getContent()
                        .get(0)
                        .getClubId()
        );

        assertEquals(
                3L,
                response.getContent()
                        .get(1)
                        .getClubId()
        );

        verify(joueurRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void getAllJoueurs_shouldApplyDescendingSort() {
        when(
                joueurRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of())
        );

        JoueurFilterRequest filters =
                new JoueurFilterRequest();

        filters.setDirection("desc");

        joueurService.getAllJoueurs(filters);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(joueurRepository)
                .findAll(
                        any(Specification.class),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        Sort.Order sortOrder =
                pageable.getSort()
                        .getOrderFor("nom");

        assertNotNull(sortOrder);
        assertEquals(
                Sort.Direction.DESC,
                sortOrder.getDirection()
        );
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void getAllJoueurs_shouldApplyCombinedFilters() {
        Club club = club(4L);

        Joueur joueur = joueur(1L, club);
        joueur.setNom("Mbappé");
        joueur.setPrenom("Kylian");
        joueur.setPostePrincipal("Attaquant");
        joueur.setNationalite("France");

        when(
                joueurRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of(joueur))
        );

        JoueurFilterRequest filters =
                new JoueurFilterRequest();

        PageResponse<JoueurResponse> response =
                joueurService.getAllJoueurs(filters);

        assertEquals(
                1,
                response.getContent().size()
        );

        assertEquals(
                "Mbappé",
                response.getContent()
                        .getFirst()
                        .getNom()
        );

        assertEquals(
                "Attaquant",
                response.getContent()
                        .getFirst()
                        .getPostePrincipal()
        );

        assertEquals(
                4L,
                response.getContent()
                        .getFirst()
                        .getClubId()
        );

        assertEquals(
                "France",
                response.getContent()
                        .getFirst()
                        .getNationalite()
        );

        verify(joueurRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void getJoueurById_shouldReturnResponse_whenJoueurExists() {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(1L, null)));

        JoueurResponse response = joueurService.getJoueurById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Mbappé", response.getNom());
    }

    @Test
    void getJoueurById_shouldThrowException_whenJoueurDoesNotExist() {
        when(joueurRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> joueurService.getJoueurById(99L)
        );

        assertEquals("Joueur introuvable avec l'id : 99", exception.getMessage());
    }

    @Test
    void updateJoueur_shouldUpdateAndReturnResponse() {
        Joueur existing = joueur(1L, null);
        Club club = club(2L);
        JoueurRequest request = request(2L);
        request.setNom("Hernández");

        when(joueurRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
        when(joueurRepository.save(existing)).thenReturn(existing);

        JoueurResponse response = joueurService.updateJoueur(1L, request);

        assertEquals("Hernández", response.getNom());
        assertEquals(2L, response.getClubId());
        verify(joueurRepository).save(existing);
    }

    @Test
    void deleteJoueur_shouldDeleteJoueur_whenJoueurExists() {
        Joueur existing = joueur(1L, null);
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(existing));

        joueurService.deleteJoueur(1L);

        verify(joueurRepository).delete(existing);
    }

    @Test
    void getAllJoueurs_shouldUseAllAvailableFilters() {
        Club club = club(4L);

        Joueur joueur = joueur(1L, club);
        joueur.setNom("JoueurTest");
        joueur.setPrenom("Europe");
        joueur.setPostePrincipal("Attaquant");
        joueur.setNationalite("France");
        joueur.setPiedFort("Droit");
        joueur.setTaille(180);
        joueur.setPoids(76);
        joueur.setDateNaissance(
                LocalDate.of(2000, 5, 10)
        );

        when(
                joueurRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of(joueur))
        );

        JoueurFilterRequest filters =
                new JoueurFilterRequest();

        filters.setPage(0);
        filters.setSize(10);
        filters.setSortBy("nom");
        filters.setDirection("asc");
        filters.setSearch("Joueur");
        filters.setClubId(4L);
        filters.setPoste("Attaquant");
        filters.setNationalite("France");
        filters.setPiedFort("Droit");
        filters.setTailleMin(175);
        filters.setTailleMax(185);
        filters.setPoidsMin(70);
        filters.setPoidsMax(80);
        filters.setDateNaissanceMin(
                LocalDate.of(1999, 1, 1)
        );
        filters.setDateNaissanceMax(
                LocalDate.of(2001, 12, 31)
        );

        PageResponse<JoueurResponse> response =
                joueurService.getAllJoueurs(filters);

        assertEquals(1, response.getContent().size());

        assertEquals(
                "JoueurTest",
                response.getContent()
                        .getFirst()
                        .getNom()
        );

        assertEquals(
                "Droit",
                response.getContent()
                        .getFirst()
                        .getPiedFort()
        );

        assertEquals(
                180,
                response.getContent()
                        .getFirst()
                        .getTaille()
        );

        assertEquals(
                76,
                response.getContent()
                        .getFirst()
                        .getPoids()
        );

        verify(joueurRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void getAllJoueurs_shouldRejectInvalidHeightRange() {
        JoueurFilterRequest filters =
                new JoueurFilterRequest();

        filters.setTailleMin(190);
        filters.setTailleMax(175);

        InvalidFilterException exception =
                assertThrows(
                        InvalidFilterException.class,
                        () -> joueurService
                                .getAllJoueurs(filters)
                );

        assertEquals(
                "La taille minimale ne doit pas être supérieure "
                        + "à la taille maximale.",
                exception.getMessage()
        );

        verifyNoInteractions(joueurRepository);
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

    private Joueur joueur(Long id, Club club) {
        return Joueur.builder()
                .id(id)
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

    private Club club(Long id) {
        return Club.builder()
                .id(id)
                .nom("Arsenal FC")
                .pays("Angleterre")
                .build();
    }
}
