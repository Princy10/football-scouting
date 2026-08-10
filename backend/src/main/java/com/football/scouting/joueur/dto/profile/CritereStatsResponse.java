package com.football.scouting.joueur.dto.profile;

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
public class CritereStatsResponse {

    private String critere;

    private Double moyenne;

    private Integer noteMinimum;

    private Integer noteMaximum;

    private int nombreNotes;
}