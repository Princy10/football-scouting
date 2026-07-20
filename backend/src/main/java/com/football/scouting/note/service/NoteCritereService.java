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
import java.util.Objects;

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

        /*
         * saveAndFlush permet d'envoyer immédiatement la note
         * dans PostgreSQL avant de calculer la moyenne.
         */
        NoteCritere savedNote =
                noteCritereRepository.saveAndFlush(noteCritere);

        recalculateScoreGlobal(rapport);

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
        return mapToResponse(findNoteById(id));
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

        RapportScouting ancienRapport =
                noteCritere.getRapport();

        RapportScouting nouveauRapport =
                findRapportById(request.getRapportId());

        noteCritere.setRapport(nouveauRapport);
        noteCritere.setCritere(request.getCritere());
        noteCritere.setNoteSur100(request.getNoteSur100());

        NoteCritere updatedNote =
                noteCritereRepository.saveAndFlush(noteCritere);

        /*
         * Si la note a été transférée vers un autre rapport,
         * il faut recalculer l'ancien rapport.
         */
        if (!Objects.equals(
                ancienRapport.getId(),
                nouveauRapport.getId()
        )) {
            recalculateScoreGlobal(ancienRapport);
        }

        recalculateScoreGlobal(nouveauRapport);

        return mapToResponse(updatedNote);
    }

    public void deleteNote(Long id) {
        NoteCritere noteCritere = findNoteById(id);

        RapportScouting rapport =
                noteCritere.getRapport();

        noteCritereRepository.delete(noteCritere);

        /*
         * Force l'exécution du DELETE avant le calcul AVG.
         */
        noteCritereRepository.flush();

        recalculateScoreGlobal(rapport);
    }

    private void recalculateScoreGlobal(
            RapportScouting rapport
    ) {
        Double moyenne =
                noteCritereRepository
                        .calculateAverageByRapportId(
                                rapport.getId()
                        );

        if (moyenne == null) {
            rapport.setScoreGlobal(null);
        } else {
            rapport.setScoreGlobal(
                    (int) Math.round(moyenne)
            );
        }

        rapportScoutingRepository.save(rapport);
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
                .rapportId(
                        noteCritere.getRapport().getId()
                )
                .critere(noteCritere.getCritere())
                .noteSur100(noteCritere.getNoteSur100())
                .build();
    }
}