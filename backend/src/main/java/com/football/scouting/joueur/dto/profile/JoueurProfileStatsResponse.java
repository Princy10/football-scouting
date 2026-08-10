package com.football.scouting.joueur.dto.profile;

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
public class JoueurProfileStatsResponse {

    private int nombreRapports;

    private int nombreNotes;

    private int nombreCriteresDistincts;

    private Double scoreMoyen;

    private Integer scoreMinimum;

    private Integer scoreMaximum;

    private Integer dernierScore;

    private LocalDate dateDerniereObservation;
}