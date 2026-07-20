package com.football.scouting.note.service;

import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.note.dto.NoteCritereRequest;
import com.football.scouting.note.dto.NoteCritereResponse;
import com.football.scouting.note.entity.NoteCritere;
import com.football.scouting.note.repository.NoteCritereRepository;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteCritereServiceTest {

    @Mock
    private NoteCritereRepository noteCritereRepository;

    @Mock
    private RapportScoutingRepository rapportScoutingRepository;

    @InjectMocks
    private NoteCritereService noteCritereService;

    @Test
    void createNote_shouldCreateAndReturnResponse() {
        RapportScouting rapport = rapport(1L);

        NoteCritere savedNote = note(
                10L,
                rapport,
                "Technique",
                85
        );

        when(rapportScoutingRepository.findById(1L))
                .thenReturn(Optional.of(rapport));

        when(noteCritereRepository.save(
                any(NoteCritere.class)
        )).thenReturn(savedNote);

        NoteCritereResponse response =
                noteCritereService.createNote(request(1L));

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getRapportId());
        assertEquals("Technique", response.getCritere());
        assertEquals(85, response.getNoteSur100());

        verify(rapportScoutingRepository).findById(1L);
        verify(noteCritereRepository)
                .save(any(NoteCritere.class));
    }

    @Test
    void createNote_shouldThrowException_whenRapportDoesNotExist() {
        when(rapportScoutingRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> noteCritereService
                                .createNote(request(99L))
                );

        assertEquals(
                "Rapport de scouting introuvable avec l'id : 99",
                exception.getMessage()
        );
    }

    @Test
    void getAllNotes_shouldReturnResponses() {
        when(noteCritereRepository.findAllByOrderByIdDesc())
                .thenReturn(List.of(
                        note(
                                2L,
                                rapport(20L),
                                "Vitesse",
                                90
                        ),
                        note(
                                1L,
                                rapport(10L),
                                "Technique",
                                85
                        )
                ));

        List<NoteCritereResponse> responses =
                noteCritereService.getAllNotes();

        assertEquals(2, responses.size());

        assertEquals(
                20L,
                responses.get(0).getRapportId()
        );

        assertEquals(
                "Vitesse",
                responses.get(0).getCritere()
        );

        assertEquals(
                10L,
                responses.get(1).getRapportId()
        );

        verify(noteCritereRepository)
                .findAllByOrderByIdDesc();
    }

    @Test
    void getNoteById_shouldReturnResponse_whenNoteExists() {
        when(noteCritereRepository.findById(1L))
                .thenReturn(Optional.of(
                        note(
                                1L,
                                rapport(2L),
                                "Passe",
                                88
                        )
                ));

        NoteCritereResponse response =
                noteCritereService.getNoteById(1L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getRapportId());
        assertEquals("Passe", response.getCritere());
        assertEquals(88, response.getNoteSur100());
    }

    @Test
    void getNoteById_shouldThrowException_whenNoteDoesNotExist() {
        when(noteCritereRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> noteCritereService
                                .getNoteById(99L)
                );

        assertEquals(
                "Note de critère introuvable avec l'id : 99",
                exception.getMessage()
        );
    }

    @Test
    void getNotesByRapport_shouldReturnResponses() {
        RapportScouting rapport = rapport(1L);

        when(rapportScoutingRepository.findById(1L))
                .thenReturn(Optional.of(rapport));

        when(
                noteCritereRepository
                        .findByRapport_IdOrderByIdAsc(1L)
        ).thenReturn(List.of(
                note(
                        1L,
                        rapport,
                        "Technique",
                        85
                ),
                note(
                        2L,
                        rapport,
                        "Vitesse",
                        90
                )
        ));

        List<NoteCritereResponse> responses =
                noteCritereService.getNotesByRapport(1L);

        assertEquals(2, responses.size());
        assertEquals(
                "Technique",
                responses.get(0).getCritere()
        );
        assertEquals(
                "Vitesse",
                responses.get(1).getCritere()
        );

        verify(rapportScoutingRepository).findById(1L);

        verify(noteCritereRepository)
                .findByRapport_IdOrderByIdAsc(1L);
    }

    @Test
    void getNotesByRapport_shouldThrowException_whenRapportDoesNotExist() {
        when(rapportScoutingRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> noteCritereService
                                .getNotesByRapport(99L)
                );

        assertEquals(
                "Rapport de scouting introuvable avec l'id : 99",
                exception.getMessage()
        );
    }

    @Test
    void updateNote_shouldUpdateAndReturnResponse() {
        NoteCritere existing = note(
                1L,
                rapport(1L),
                "Technique",
                70
        );

        RapportScouting nouveauRapport = rapport(2L);

        NoteCritereRequest request =
                NoteCritereRequest.builder()
                        .rapportId(2L)
                        .critere("Vitesse")
                        .noteSur100(92)
                        .build();

        when(noteCritereRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(rapportScoutingRepository.findById(2L))
                .thenReturn(Optional.of(nouveauRapport));

        when(noteCritereRepository.save(existing))
                .thenReturn(existing);

        NoteCritereResponse response =
                noteCritereService.updateNote(
                        1L,
                        request
                );

        assertEquals(2L, response.getRapportId());
        assertEquals(
                "Vitesse",
                response.getCritere()
        );
        assertEquals(92, response.getNoteSur100());

        verify(noteCritereRepository).save(existing);
    }

    @Test
    void deleteNote_shouldDeleteNote_whenNoteExists() {
        NoteCritere existing = note(
                1L,
                rapport(1L),
                "Technique",
                85
        );

        when(noteCritereRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        noteCritereService.deleteNote(1L);

        verify(noteCritereRepository).delete(existing);
    }

    private NoteCritereRequest request(Long rapportId) {
        return NoteCritereRequest.builder()
                .rapportId(rapportId)
                .critere("Technique")
                .noteSur100(85)
                .build();
    }

    private NoteCritere note(
            Long id,
            RapportScouting rapport,
            String critere,
            Integer noteSur100
    ) {
        return NoteCritere.builder()
                .id(id)
                .rapport(rapport)
                .critere(critere)
                .noteSur100(noteSur100)
                .build();
    }

    private RapportScouting rapport(Long id) {
        return RapportScouting.builder()
                .id(id)
                .build();
    }
}