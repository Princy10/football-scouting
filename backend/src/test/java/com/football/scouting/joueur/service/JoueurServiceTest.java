package com.football.scouting.joueur.service;

import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.entity.Joueur;
import com.football.scouting.joueur.repository.JoueurRepository;
import com.football.scouting.common.dto.PageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

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
        assertEquals("Rakoto", response.getNom());
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

        PageResponse<JoueurResponse> response =
                joueurService.getAllJoueurs(
                        0,
                        10,
                        "nom",
                        "asc",
                        null
                );

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
                .findAll(any(Pageable.class));
    }

    @Test
    void getAllJoueurs_shouldApplyDescendingSort() {
        when(
                joueurRepository.findAll(
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of())
        );

        joueurService.getAllJoueurs(
                0,
                10,
                "nom",
                "desc",
                null
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(joueurRepository)
                .findAll(pageableCaptor.capture());

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
    void getAllJoueurs_shouldSearchByNameOrFirstName() {
        Joueur joueur = joueur(
                1L,
                club(4L)
        );

        joueur.setNom("Mbappé");
        joueur.setPrenom("Kylian");

        when(
                joueurRepository
                        .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
                                eq("mbapp"),
                                eq("mbapp"),
                                any(Pageable.class)
                        )
        ).thenReturn(
                new PageImpl<>(List.of(joueur))
        );

        PageResponse<JoueurResponse> response =
                joueurService.getAllJoueurs(
                        0,
                        10,
                        "nom",
                        "asc",
                        "  mbapp  "
                );

        assertEquals(1, response.getContent().size());
        assertEquals(
                "Mbappé",
                response.getContent()
                        .get(0)
                        .getNom()
        );

        assertEquals(
                "Kylian",
                response.getContent()
                        .get(0)
                        .getPrenom()
        );

        verify(joueurRepository)
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
                        eq("mbapp"),
                        eq("mbapp"),
                        any(Pageable.class)
                );
    }

    @Test
    void getAllJoueurs_shouldUseFindAll_whenSearchIsBlank() {
        when(
                joueurRepository.findAll(
                        any(Pageable.class)
                )
        ).thenReturn(
                new PageImpl<>(List.of())
        );

        joueurService.getAllJoueurs(
                0,
                10,
                "nom",
                "asc",
                "   "
        );

        verify(joueurRepository)
                .findAll(any(Pageable.class));
    }

    @Test
    void getJoueurById_shouldReturnResponse_whenJoueurExists() {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(1L, null)));

        JoueurResponse response = joueurService.getJoueurById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Rakoto", response.getNom());
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
        request.setNom("Rakotoarisoa");

        when(joueurRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club));
        when(joueurRepository.save(existing)).thenReturn(existing);

        JoueurResponse response = joueurService.updateJoueur(1L, request);

        assertEquals("Rakotoarisoa", response.getNom());
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

    private Joueur joueur(Long id, Club club) {
        return Joueur.builder()
                .id(id)
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

    private Club club(Long id) {
        return Club.builder().id(id).nom("Ajesaia").pays("Madagascar").build();
    }
}
