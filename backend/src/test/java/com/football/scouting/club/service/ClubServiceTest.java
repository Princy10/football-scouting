package com.football.scouting.club.service;

import com.football.scouting.club.dto.ClubRequest;
import com.football.scouting.club.dto.ClubResponse;
import com.football.scouting.club.entity.Club;
import com.football.scouting.club.repository.ClubRepository;
import com.football.scouting.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    @Test
    void createClub_shouldCreateAndReturnClubResponse() {
        ClubRequest request = ClubRequest.builder()
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build();

        Club savedClub = Club.builder()
                .id(1L)
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build();

        when(clubRepository.save(any(Club.class))).thenReturn(savedClub);

        ClubResponse response = clubService.createClub(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Ajesaia", response.getNom());
        assertEquals("Madagascar", response.getPays());
        assertEquals("Antananarivo", response.getVille());
        assertEquals("D1", response.getDivision());

        verify(clubRepository).save(any(Club.class));
    }

    @Test
    void getAllClubs_shouldReturnListOfClubResponses() {
        List<Club> clubs = List.of(
                Club.builder().id(1L).nom("Ajesaia").pays("Madagascar").ville("Antananarivo").division("D1").build(),
                Club.builder().id(2L).nom("Elgeco Plus").pays("Madagascar").ville("Antananarivo").division("D1").build()
        );

        when(clubRepository.findAll()).thenReturn(clubs);

        List<ClubResponse> responses = clubService.getAllClubs();

        assertEquals(2, responses.size());
        assertEquals("Ajesaia", responses.get(0).getNom());
        assertEquals("Elgeco Plus", responses.get(1).getNom());

        verify(clubRepository).findAll();
    }

    @Test
    void getClubById_shouldReturnClubResponse_whenClubExists() {
        Club club = Club.builder()
                .id(1L)
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build();

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

        ClubResponse response = clubService.getClubById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Ajesaia", response.getNom());

        verify(clubRepository).findById(1L);
    }

    @Test
    void getClubById_shouldThrowException_whenClubDoesNotExist() {
        when(clubRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clubService.getClubById(99L)
        );

        assertEquals("Club introuvable avec l'id : 99", exception.getMessage());
        verify(clubRepository).findById(99L);
    }

    @Test
    void updateClub_shouldUpdateAndReturnClubResponse() {
        ClubRequest request = ClubRequest.builder()
                .nom("Ajesaia FC")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1 Elite")
                .build();

        Club existingClub = Club.builder()
                .id(1L)
                .nom("Ajesaia")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1")
                .build();

        Club updatedClub = Club.builder()
                .id(1L)
                .nom("Ajesaia FC")
                .pays("Madagascar")
                .ville("Antananarivo")
                .division("D1 Elite")
                .build();

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenReturn(updatedClub);

        ClubResponse response = clubService.updateClub(1L, request);

        assertNotNull(response);
        assertEquals("Ajesaia FC", response.getNom());
        assertEquals("D1 Elite", response.getDivision());

        verify(clubRepository).findById(1L);
        verify(clubRepository).save(existingClub);
    }

    @Test
    void deleteClub_shouldDeleteClub_whenClubExists() {
        Club existingClub = Club.builder()
                .id(1L)
                .nom("Ajesaia")
                .pays("Madagascar")
                .build();

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));

        clubService.deleteClub(1L);

        verify(clubRepository).findById(1L);
        verify(clubRepository).delete(existingClub);
    }
}