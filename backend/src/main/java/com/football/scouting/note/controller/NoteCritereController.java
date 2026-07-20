package com.football.scouting.note.controller;

import com.football.scouting.note.dto.NoteCritereRequest;
import com.football.scouting.note.dto.NoteCritereResponse;
import com.football.scouting.note.service.NoteCritereService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(
        name = "Notes de critères",
        description = "Gestion des notes attribuées aux critères de scouting"
)
public class NoteCritereController {

    private final NoteCritereService noteCritereService;

    @Operation(summary = "Créer une note de critère")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteCritereResponse createNote(
            @Valid @RequestBody NoteCritereRequest request
    ) {
        return noteCritereService.createNote(request);
    }

    @Operation(summary = "Lister toutes les notes de critères")
    @GetMapping
    public List<NoteCritereResponse> getAllNotes() {
        return noteCritereService.getAllNotes();
    }

    @Operation(
            summary = "Récupérer une note par son identifiant"
    )
    @GetMapping("/{id}")
    public NoteCritereResponse getNoteById(
            @PathVariable Long id
    ) {
        return noteCritereService.getNoteById(id);
    }

    @Operation(
            summary = "Lister les notes d'un rapport de scouting"
    )
    @GetMapping("/rapport/{rapportId}")
    public List<NoteCritereResponse> getNotesByRapport(
            @PathVariable Long rapportId
    ) {
        return noteCritereService
                .getNotesByRapport(rapportId);
    }

    @Operation(summary = "Modifier une note de critère")
    @PutMapping("/{id}")
    public NoteCritereResponse updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteCritereRequest request
    ) {
        return noteCritereService.updateNote(id, request);
    }

    @Operation(summary = "Supprimer une note de critère")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(
            @PathVariable Long id
    ) {
        noteCritereService.deleteNote(id);
    }
}