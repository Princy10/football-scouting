package com.football.scouting.joueur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoueurRequest {

    @NotBlank(message = "Le nom du joueur est obligatoire.")
    @Size(max = 100, message = "Le nom du joueur ne doit pas dépasser 100 caractères.")
    private String nom;

    @Size(max = 100, message = "Le prénom du joueur ne doit pas dépasser 100 caractères.")
    private String prenom;

    private LocalDate dateNaissance;

    @Size(max = 100, message = "La nationalité ne doit pas dépasser 100 caractères.")
    private String nationalite;

    @NotBlank(message = "Le poste principal est obligatoire.")
    @Size(max = 100, message = "Le poste principal ne doit pas dépasser 100 caractères.")
    private String postePrincipal;

    @Size(max = 20, message = "Le pied fort ne doit pas dépasser 20 caractères.")
    private String piedFort;

    @Positive(message = "La taille doit être strictement positive.")
    private Integer taille;

    @Positive(message = "Le poids doit être strictement positif.")
    private Integer poids;

    private Long clubId;
}
