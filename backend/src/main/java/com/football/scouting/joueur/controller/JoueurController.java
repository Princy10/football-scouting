package com.football.scouting.joueur.controller;

import com.football.scouting.common.dto.PageResponse;
import com.football.scouting.joueur.dto.JoueurRequest;
import com.football.scouting.joueur.dto.JoueurResponse;
import com.football.scouting.joueur.service.JoueurService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
            summary = "Lister les joueurs avec pagination"
    )
    @GetMapping
    public PageResponse<JoueurResponse> getAllJoueurs(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return joueurService.getAllJoueurs(
                page,
                size
        );
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