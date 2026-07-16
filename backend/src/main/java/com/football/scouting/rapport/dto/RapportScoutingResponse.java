package com.football.scouting.rapport.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapportScoutingResponse {

    private Long id;

    private Long joueurId;

    private LocalDate dateObservation;

    private String matchObserve;

    private String commentaireGeneral;

    private String recommandation;

    private Integer scoreGlobal;

    private String scoutName;

    private LocalDateTime createdAt;
}