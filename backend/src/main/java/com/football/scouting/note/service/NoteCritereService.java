package com.football.scouting.note.service;

import com.football.scouting.common.exception.ResourceNotFoundException;
import com.football.scouting.note.dto.NoteCritereRequest;
import com.football.scouting.note.dto.NoteCritereResponse;
import com.football.scouting.note.entity.NoteCritere;
import com.football.scouting.note.repository.NoteCritereRepository;
import com.football.scouting.rapport.entity.RapportScouting;
import com.football.scouting.rapport.repository.RapportScoutingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteCritereService {

    private final NoteCritereRepository noteCritereRepository;
    private final RapportScoutingRepository rapportScoutingRepository;

    public NoteCritereResponse createNote(
            NoteCritereRequest request
    ) {
        RapportScouting rapport =
                findRapportById(request.getRapportId());

        NoteCritere noteCritere = NoteCritere.builder()
                .rapport(rapport)
                .critere(request.getCritere())
                .noteSur100(request.getNoteSur100())
                .build();

        NoteCritere savedNote =
                noteCritereRepository.save(noteCritere);

        return mapToResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public List<NoteCritereResponse> getAllNotes() {
        return noteCritereRepository
                .findAllByOrderByIdDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteCritereResponse getNoteById(Long id) {
        NoteCritere noteCritere = findNoteById(id);

        return mapToResponse(noteCritere);
    }

    @Transactional(readOnly = true)
    public List<NoteCritereResponse> getNotesByRapport(
            Long rapportId
    ) {
        findRapportById(rapportId);

        return noteCritereRepository
                .findByRapport_IdOrderByIdAsc(rapportId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NoteCritereResponse updateNote(
            Long id,
            NoteCritereRequest request
    ) {
        NoteCritere noteCritere = findNoteById(id);

        RapportScouting rapport =
                findRapportById(request.getRapportId());

        noteCritere.setRapport(rapport);
        noteCritere.setCritere(request.getCritere());
        noteCritere.setNoteSur100(request.getNoteSur100());

        NoteCritere updatedNote =
                noteCritereRepository.save(noteCritere);

        return mapToResponse(updatedNote);
    }

    public void deleteNote(Long id) {
        NoteCritere noteCritere = findNoteById(id);

        noteCritereRepository.delete(noteCritere);
    }

    private NoteCritere findNoteById(Long id) {
        return noteCritereRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Note de critère introuvable avec l'id : "
                                        + id
                        )
                );
    }

    private RapportScouting findRapportById(
            Long rapportId
    ) {
        return rapportScoutingRepository
                .findById(rapportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rapport de scouting introuvable avec l'id : "
                                        + rapportId
                        )
                );
    }

    private NoteCritereResponse mapToResponse(
            NoteCritere noteCritere
    ) {
        return NoteCritereResponse.builder()
                .id(noteCritere.getId())
                .rapportId(noteCritere.getRapport().getId())
                .critere(noteCritere.getCritere())
                .noteSur100(noteCritere.getNoteSur100())
                .build();
    }
}