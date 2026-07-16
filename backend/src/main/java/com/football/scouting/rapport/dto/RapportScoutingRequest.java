package com.football.scouting.rapport.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportScoutingRequest {

    @NotNull(message = "L'identifiant du joueur est obligatoire.")
    @Positive(message = "L'identifiant du joueur doit être supérieur à zéro.")
    private Long joueurId;

    @NotNull(message = "La date d'observation est obligatoire.")
    @PastOrPresent(message = "La date d'observation ne peut pas être dans le futur.")
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

    @Min(
            value = 0,
            message = "Le score global doit être supérieur ou égal à 0."
    )
    @Max(
            value = 100,
            message = "Le score global doit être inférieur ou égal à 100."
    )
    private Integer scoreGlobal;

    @Size(
            max = 150,
            message = "Le nom du scout ne doit pas dépasser 150 caractères."
    )
    private String scoutName;
}