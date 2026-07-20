package com.football.scouting.note.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class NoteCritereRequest {

    @NotNull(message = "L'identifiant du rapport est obligatoire.")
    @Positive(message = "L'identifiant du rapport doit être supérieur à zéro.")
    private Long rapportId;

    @NotBlank(message = "Le critère est obligatoire.")
    @Size(
            max = 100,
            message = "Le critère ne doit pas dépasser 100 caractères."
    )
    private String critere;

    @NotNull(message = "La note est obligatoire.")
    @Min(
            value = 0,
            message = "La note doit être supérieure ou égale à 0."
    )
    @Max(
            value = 100,
            message = "La note doit être inférieure ou égale à 100."
    )
    private Integer noteSur100;
}