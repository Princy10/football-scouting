package com.football.scouting.joueur.dto.profile;

import com.football.scouting.note.dto.NoteCritereResponse;
import com.football.scouting.rapport.dto.RapportScoutingResponse;
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
public class RapportDetailResponse {

    private RapportScoutingResponse rapport;

    private List<NoteCritereResponse> notes;
}