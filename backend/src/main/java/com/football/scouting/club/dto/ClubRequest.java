package com.football.scouting.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubRequest {

    @NotBlank(message = "Le nom du club est obligatoire.")
    @Size(max = 150, message = "Le nom du club ne doit pas dépasser 150 caractères.")
    private String nom;

    @NotBlank(message = "Le pays est obligatoire.")
    @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères.")
    private String pays;

    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères.")
    private String ville;

    @Size(max = 100, message = "La division ne doit pas dépasser 100 caractères.")
    private String division;
}