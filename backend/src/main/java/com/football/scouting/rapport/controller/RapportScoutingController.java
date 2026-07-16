package com.football.scouting.rapport.controller;

import com.football.scouting.rapport.dto.RapportScoutingRequest;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
import com.football.scouting.rapport.service.RapportScoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
@Tag(
        name = "Rapport de scouting",
        description = "Gestion des rapports de scouting"
)
public class RapportScoutingController {

    private final RapportScoutingService rapportService;

    @Operation(summary = "Créer un rapport de scouting")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RapportScoutingResponse createRapport(
            @Valid @RequestBody RapportScoutingRequest request
    ) {
        return rapportService.createRapport(request);
    }

    @Operation(summary = "Lister tous les rapports de scouting")
    @GetMapping
    public List<RapportScoutingResponse> getAllRapports() {
        return rapportService.getAllRapports();
    }

    @Operation(
            summary = "Récupérer un rapport par son identifiant"
    )
    @GetMapping("/{id}")
    public RapportScoutingResponse getRapportById(
            @PathVariable Long id
    ) {
        return rapportService.getRapportById(id);
    }

    @Operation(
            summary = "Lister les rapports d'un joueur"
    )
    @GetMapping("/joueur/{joueurId}")
    public List<RapportScoutingResponse> getRapportsByJoueur(
            @PathVariable Long joueurId
    ) {
        return rapportService.getRapportsByJoueur(joueurId);
    }

    @Operation(summary = "Modifier un rapport de scouting")
    @PutMapping("/{id}")
    public RapportScoutingResponse updateRapport(
            @PathVariable Long id,
            @Valid @RequestBody RapportScoutingRequest request
    ) {
        return rapportService.updateRapport(id, request);
    }

    @Operation(summary = "Supprimer un rapport de scouting")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRapport(@PathVariable Long id) {
        rapportService.deleteRapport(id);
    }
}