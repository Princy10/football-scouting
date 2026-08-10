package com.football.scouting.joueur.dto.profile;

import com.football.scouting.club.dto.ClubResponse;
import com.football.scouting.joueur.dto.JoueurResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoueurProfileResponse {

    private JoueurResponse joueur;

    private ClubResponse club;

    private JoueurProfileStatsResponse statistiques;

    private List<CritereStatsResponse> criteres;

    private List<RapportDetailResponse> rapports;
}