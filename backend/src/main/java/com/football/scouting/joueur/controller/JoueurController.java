package com.football.scouting.joueur.controller;

import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.joueur.dto.JoueurFilterRequest;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.service.JoueurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/joueurs")
@RequiredArgsConstructor
@Tag(
        name = "Joueur",
        description = "Gestion des joueurs"
)
public class JoueurController {

    private final JoueurService joueurService;

    @Operation(summary = "Créer un joueur")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JoueurResponse createJoueur(
            @Valid @RequestBody JoueurRequest request
    ) {
        return joueurService.createJoueur(request);
    }

    @Operation(
            summary = "Lister, rechercher, filtrer et trier les joueurs"
    )
    @GetMapping
    public PageResponse<JoueurResponse> getAllJoueurs(
            @ParameterObject
            @ModelAttribute
            JoueurFilterRequest filters
    ) {
        return joueurService.getAllJoueurs(filters);
    }

    @Operation(
            summary = "Récupérer un joueur par son identifiant"
    )
    @GetMapping("/{id}")
    public JoueurResponse getJoueurById(
            @PathVariable Long id
    ) {
        return joueurService.getJoueurById(id);
    }

    @Operation(summary = "Mettre à jour un joueur")
    @PutMapping("/{id}")
    public JoueurResponse updateJoueur(
            @PathVariable Long id,
            @Valid @RequestBody JoueurRequest request
    ) {
        return joueurService.updateJoueur(
                id,
                request
        );
    }

    @Operation(summary = "Supprimer un joueur")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJoueur(
            @PathVariable Long id
    ) {
        joueurService.deleteJoueur(id);
    }
}