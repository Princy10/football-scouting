package com.football.scouting.rapport.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
public class RapportScoutingRequest {

    @NotNull(
            message = "L'identifiant du joueur est obligatoire."
    )
    @Positive(
            message = "L'identifiant du joueur doit être supérieur à zéro."
    )
    private Long joueurId;

    @NotNull(
            message = "La date d'observation est obligatoire."
    )
    @PastOrPresent(
            message = "La date d'observation ne peut pas être dans le futur."
    )
    private LocalDate dateObservation;

    @Size(
            max = 255,
            message = "Le match observé ne doit pas dépasser 255 caractères."
    )
    private String matchObserve;

    private String commentaireGeneral;

    @Size(
            max = 100,
            message = "La recommandation ne doit pas dépasser 100 caractères."
    )
    private String recommandation;

    @Size(
            max = 150,
            message = "Le nom du scout ne doit pas dépasser 150 caractères."
    )
    private String scoutName;
}