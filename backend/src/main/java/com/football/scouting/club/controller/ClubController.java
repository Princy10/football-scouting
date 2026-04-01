package com.football.scouting.club.controller;

import com.football.scouting.club.dto.ClubRequest;
import com.football.scouting.club.dto.ClubResponse;
import com.football.scouting.club.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "Club", description = "Gestion des clubs")
public class ClubController {

    private final ClubService clubService;

    @Operation(summary = "Creer un club")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubResponse createClub(@Valid @RequestBody ClubRequest request) {
        return clubService.createClub(request);
    }

    @Operation(summary = "Lister tous les clubs")
    @GetMapping
    public List<ClubResponse> getAllClubs() {
        return clubService.getAllClubs();
    }

    @Operation(summary = "Recuperer un club par son identifiant")
    @GetMapping("/{id}")
    public ClubResponse getClubById(@PathVariable Long id) {
        return clubService.getClubById(id);
    }

    @Operation(summary = "Mettre a jour un club")
    @PutMapping("/{id}")
    public ClubResponse updateClub(@PathVariable Long id, @Valid @RequestBody ClubRequest request) {
        return clubService.updateClub(id, request);
    }

    @Operation(summary = "Supprimer un club")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
    }
}
